package com.monkeys.shared.exception

import com.monkeys.shared.dto.BaseResponse
import com.monkeys.shared.dto.ErrorInfo
import com.monkeys.shared.util.ApiException
import com.monkeys.shared.util.RetryExhaustedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import tools.jackson.core.JacksonException
import java.util.concurrent.TimeoutException

/**
 * 전역 예외 처리 핸들러
 * 모든 컨트롤러에서 발생하는 예외를 일관된 형식으로 처리
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * API 관련 예외 처리
     */
    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<BaseResponse<Any>> {
        logger.error("API 예외 발생: {} (코드: {})", e.message, e.errorCode, e)
        
        val status = when (e.errorCode) {
            "INVALID_API_KEY", "AUTH_FAILED" -> HttpStatus.UNAUTHORIZED
            "BAD_REQUEST", "INVALID_PARAMETER" -> HttpStatus.BAD_REQUEST
            "NOT_FOUND" -> HttpStatus.NOT_FOUND
            "RATE_LIMIT_EXCEEDED" -> HttpStatus.TOO_MANY_REQUESTS
            "SERVER_ERROR" -> HttpStatus.INTERNAL_SERVER_ERROR
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity.status(status).body(
            BaseResponse.failure<Any>(
                message = e.message ?: "API 오류가 발생했습니다",
                errorCode = e.errorCode
            )
        )
    }

    /**
     * 재시도 횟수 소진 예외 처리
     */
    @ExceptionHandler(RetryExhaustedException::class)
    fun handleRetryExhaustedException(e: RetryExhaustedException): ResponseEntity<BaseResponse<Any>> {
        logger.error("재시도 횟수 소진: {}", e.message, e)
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            BaseResponse.failure<Any>(
                message = "서비스 일시 장애: ${e.message}",
                errorCode = "SERVICE_UNAVAILABLE"
            )
        )
    }

    /**
     * JSON 파싱 예외 처리
     */
    @ExceptionHandler(JacksonException::class)
    fun handleJacksonException(e: JacksonException): ResponseEntity<BaseResponse<Any>> {
        logger.error("JSON 파싱 예외: {}", e.message, e)
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            BaseResponse.failure<Any>(
                message = "잘못된 JSON 형식입니다",
                errorCode = "INVALID_JSON"
            )
        )
    }

    /**
     * 유효성 검증 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<BaseResponse<Any>> {
        val fieldErrors = e.bindingResult.fieldErrors.associate { 
            it.field to (it.defaultMessage ?: "유효하지 않은 값") 
        }
        
        logger.warn("유효성 검증 실패: {}", fieldErrors)
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            BaseResponse.failure<Any>(
                message = "입력값 검증 실패",
                errorCode = "VALIDATION_FAILED"
            ).copy(
                error = ErrorInfo(
                    code = "VALIDATION_FAILED",
                    message = "입력값 검증 실패",
                    details = fieldErrors
                )
            )
        )
    }

    /**
     * 타입 변환 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<BaseResponse<Any>> {
        logger.warn("타입 변환 예외: 파라미터 '{}' 값 '{}'을(를) {}로 변환할 수 없음", 
            e.name, e.value, e.requiredType?.simpleName)
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            BaseResponse.failure<Any>(
                message = "잘못된 파라미터 타입: ${e.name}",
                errorCode = "INVALID_PARAMETER_TYPE"
            )
        )
    }

    /**
     * 타임아웃 예외 처리
     */
    @ExceptionHandler(TimeoutException::class)
    fun handleTimeoutException(e: TimeoutException): ResponseEntity<BaseResponse<Any>> {
        logger.error("타임아웃 예외: {}", e.message, e)
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(
            BaseResponse.failure<Any>(
                message = "요청 처리 시간 초과",
                errorCode = "TIMEOUT"
            )
        )
    }

    /**
     * 일반적인 런타임 예외 처리
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<BaseResponse<Any>> {
        logger.error("런타임 예외 발생: {}", e.message, e)
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            BaseResponse.failure<Any>(
                message = "내부 서버 오류가 발생했습니다",
                errorCode = "INTERNAL_SERVER_ERROR"
            )
        )
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<BaseResponse<Any>> {
        logger.error("예상치 못한 예외 발생: {}", e.message, e)
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            BaseResponse.failure<Any>(
                message = "예상치 못한 오류가 발생했습니다",
                errorCode = "UNEXPECTED_ERROR"
            )
        )
    }
}

/**
 * 비즈니스 로직 예외
 */
open class BusinessException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 리소스를 찾을 수 없음 예외
 */
class ResourceNotFoundException(
    message: String,
    errorCode: String = "RESOURCE_NOT_FOUND"
) : BusinessException(message, errorCode)

/**
 * 권한 없음 예외
 */
class UnauthorizedException(
    message: String,
    errorCode: String = "UNAUTHORIZED"
) : BusinessException(message, errorCode)

/**
 * 잘못된 요청 예외
 */
class BadRequestException(
    message: String,
    errorCode: String = "BAD_REQUEST"
) : BusinessException(message, errorCode)