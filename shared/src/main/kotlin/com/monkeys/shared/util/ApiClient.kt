package com.monkeys.shared.util

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer

/**
 * 공통 API 클라이언트 유틸리티
 * 모든 외부 API 호출을 위한 표준화된 HTTP 클라이언트
 */
class ApiClient(
    private val httpClient: OkHttpClient,
    private val meterRegistry: MeterRegistry,
    private val serviceName: String
) {
    private val mapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(ApiClient::class.java)
    
    private val apiTimer = Timer.builder("api.request")
        .description("$serviceName API 요청 시간")
        .tag("service", serviceName)
        .register(meterRegistry)

    /**
     * GET 요청 실행
     */
    fun get(url: String, headers: Map<String, String> = emptyMap()): ApiResponse {
        val request = Request.Builder()
            .url(url)
            .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
            .addHeader("User-Agent", "MCP-Monkeys-$serviceName/1.0")
            .build()

        return executeRequest(request)
    }

    /**
     * POST 요청 실행
     */
    fun post(
        url: String, 
        body: Map<String, Any>, 
        headers: Map<String, String> = emptyMap()
    ): ApiResponse {
        val jsonBody = mapper.writeValueAsString(body)
        val request = Request.Builder()
            .url(url)
            .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
            .addHeader("User-Agent", "MCP-Monkeys-$serviceName/1.0")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        return executeRequest(request)
    }

    /**
     * DELETE 요청 실행
     */
    fun delete(url: String, headers: Map<String, String> = emptyMap()): ApiResponse {
        val request = Request.Builder()
            .url(url)
            .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
            .addHeader("User-Agent", "MCP-Monkeys-$serviceName/1.0")
            .delete()
            .build()

        return executeRequest(request)
    }

    /**
     * 실제 HTTP 요청 실행
     */
    private fun executeRequest(request: Request): ApiResponse {
        return apiTimer.recordCallable {
            try {
                httpClient.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    
                    when (response.code) {
                        in 200..299 -> {
                            meterRegistry.counter("api.success", "service", serviceName).increment()
                            ApiResponse.success(
                                statusCode = response.code,
                                data = if (responseBody.isNotBlank()) mapper.readValue(responseBody) else emptyMap()
                            )
                        }
                        400 -> {
                            meterRegistry.counter("api.error", "service", serviceName, "type", "bad_request").increment()
                            ApiResponse.error(response.code, "잘못된 요청", "BAD_REQUEST")
                        }
                        401, 403 -> {
                            meterRegistry.counter("api.error", "service", serviceName, "type", "auth").increment()
                            ApiResponse.error(response.code, "인증 실패", "INVALID_API_KEY")
                        }
                        404 -> {
                            meterRegistry.counter("api.error", "service", serviceName, "type", "not_found").increment()
                            ApiResponse.error(response.code, "리소스를 찾을 수 없습니다", "NOT_FOUND")
                        }
                        429 -> {
                            meterRegistry.counter("api.error", "service", serviceName, "type", "rate_limit").increment()
                            ApiResponse.error(response.code, "API 사용량 한도 초과", "RATE_LIMIT_EXCEEDED")
                        }
                        in 500..599 -> {
                            meterRegistry.counter("api.error", "service", serviceName, "type", "server_error").increment()
                            ApiResponse.error(response.code, "서버 오류", "SERVER_ERROR")
                        }
                        else -> {
                            meterRegistry.counter("api.error", "service", serviceName, "type", "unknown").increment()
                            ApiResponse.error(response.code, "알 수 없는 오류", "UNKNOWN_ERROR")
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("HTTP 요청 실행 중 오류 발생", e)
                meterRegistry.counter("api.error", "service", serviceName, "type", "network").increment()
                ApiResponse.error(0, "네트워크 오류: ${e.message}", "NETWORK_ERROR")
            }
        } ?: ApiResponse.error(0, "요청 타임아웃", "TIMEOUT")
    }
}

/**
 * API 응답 래퍼 클래스
 */
data class ApiResponse(
    val isSuccess: Boolean,
    val statusCode: Int,
    val data: Map<String, Any>,
    val errorCode: String?,
    val errorMessage: String?
) {
    companion object {
        fun success(statusCode: Int, data: Map<String, Any>): ApiResponse {
            return ApiResponse(true, statusCode, data, null, null)
        }

        fun error(statusCode: Int, message: String, errorCode: String): ApiResponse {
            return ApiResponse(false, statusCode, emptyMap(), errorCode, message)
        }
    }

    /**
     * 성공 응답인지 확인하고 데이터 반환
     */
    fun getDataOrThrow(): Map<String, Any> {
        if (isSuccess) {
            return data
        } else {
            throw ApiException(errorMessage ?: "API 호출 실패", errorCode ?: "UNKNOWN")
        }
    }
}

/**
 * API 관련 예외 클래스
 */
class ApiException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)