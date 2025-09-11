package com.monkeys.shared.util

import org.slf4j.LoggerFactory
import io.micrometer.core.instrument.MeterRegistry
import kotlin.math.pow

/**
 * 공통 재시도 핸들러 유틸리티
 * 모든 서비스에서 사용할 수 있는 표준화된 재시도 로직
 */
class RetryHandler(
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 1000,
    private val meterRegistry: MeterRegistry? = null,
    private val serviceName: String? = null
) {
    private val logger = LoggerFactory.getLogger(RetryHandler::class.java)

    /**
     * 재시도 로직을 포함한 작업 실행
     * 
     * @param operation 실행할 작업
     * @param isRetryableException 재시도 가능한 예외인지 판단하는 함수
     * @return 작업 결과
     */
    fun <T> executeWithRetry(
        operation: () -> T,
        isRetryableException: (Exception) -> Boolean = ::defaultRetryableCheck
    ): T {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                logger.warn("작업 시도 ${attempt + 1}/$maxRetries 실패: ${e.message}")
                
                // 재시도 불가능한 예외면 즉시 실패
                if (!isRetryableException(e)) {
                    logger.error("재시도 불가능한 예외 발생: ${e.javaClass.simpleName}")
                    recordFailure("not_retryable")
                    throw e
                }
                
                // 마지막 시도가 아니면 대기
                if (attempt < maxRetries - 1) {
                    val delay = calculateDelay(attempt)
                    logger.info("${delay}ms 후 재시도합니다...")
                    Thread.sleep(delay)
                }
            }
        }
        
        recordFailure("max_retries_exceeded")
        throw RetryExhaustedException(
            "작업이 $maxRetries 번 모두 실패했습니다", 
            lastException
        )
    }

    /**
     * 비동기 작업을 위한 재시도 (suspend 함수용)
     */
    suspend fun <T> executeWithRetryAsync(
        operation: suspend () -> T,
        isRetryableException: (Exception) -> Boolean = ::defaultRetryableCheck
    ): T {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                logger.warn("비동기 작업 시도 ${attempt + 1}/$maxRetries 실패: ${e.message}")
                
                if (!isRetryableException(e)) {
                    logger.error("재시도 불가능한 예외 발생: ${e.javaClass.simpleName}")
                    recordFailure("not_retryable")
                    throw e
                }
                
                if (attempt < maxRetries - 1) {
                    val delay = calculateDelay(attempt)
                    logger.info("${delay}ms 후 재시도합니다...")
                    Thread.sleep(delay) // 간단히 Thread.sleep 사용
                }
            }
        }
        
        recordFailure("max_retries_exceeded")
        throw RetryExhaustedException(
            "비동기 작업이 $maxRetries 번 모두 실패했습니다", 
            lastException
        )
    }

    /**
     * 지수 백오프 지연 시간 계산
     */
    private fun calculateDelay(attempt: Int): Long {
        return (baseDelayMs * 2.0.pow(attempt.toDouble())).toLong()
    }

    /**
     * 기본 재시도 가능 예외 판단 로직
     */
    private fun defaultRetryableCheck(exception: Exception): Boolean {
        return when (exception) {
            is ApiException -> when (exception.errorCode) {
                "RATE_LIMIT_EXCEEDED", "SERVER_ERROR", "NETWORK_ERROR", "TIMEOUT" -> true
                else -> false
            }
            is java.net.SocketTimeoutException,
            is java.net.ConnectException,
            is java.io.IOException -> true
            else -> false
        }
    }

    /**
     * 실패 메트릭 기록
     */
    private fun recordFailure(reason: String) {
        meterRegistry?.counter(
            "retry.failure", 
            "service", serviceName ?: "unknown",
            "reason", reason
        )?.increment()
    }
}

/**
 * 재시도 횟수 소진 예외
 */
class RetryExhaustedException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 재시도 설정 빌더
 */
class RetryConfig private constructor(
    val maxRetries: Int,
    val baseDelayMs: Long,
    val meterRegistry: MeterRegistry?,
    val serviceName: String?
) {
    class Builder {
        private var maxRetries: Int = 3
        private var baseDelayMs: Long = 1000
        private var meterRegistry: MeterRegistry? = null
        private var serviceName: String? = null

        fun maxRetries(retries: Int) = apply { this.maxRetries = retries }
        fun baseDelay(delayMs: Long) = apply { this.baseDelayMs = delayMs }
        fun withMetrics(registry: MeterRegistry, service: String) = apply { 
            this.meterRegistry = registry
            this.serviceName = service
        }

        fun build() = RetryConfig(maxRetries, baseDelayMs, meterRegistry, serviceName)
    }

    companion object {
        fun builder() = Builder()
    }

    fun createHandler() = RetryHandler(maxRetries, baseDelayMs, meterRegistry, serviceName)
}