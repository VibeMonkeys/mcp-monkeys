package com.monkeys.shared.util

/**
 * MCP Tool 입력 검증 유틸리티
 */
object ValidationUtils {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    private val ISBN_REGEX = Regex("^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}\$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}\$|97[89][0-9]{10}\$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}\$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]\$")

    /**
     * 문자열이 비어있지 않은지 검증
     */
    fun requireNotBlank(value: String?, fieldName: String): String {
        require(!value.isNullOrBlank()) { "${fieldName}은(는) 비어있을 수 없습니다." }
        return value.trim()
    }

    /**
     * 이메일 형식 검증
     */
    fun validateEmail(email: String?, fieldName: String = "이메일"): String {
        val trimmed = requireNotBlank(email, fieldName)
        require(EMAIL_REGEX.matches(trimmed)) { "올바른 이메일 형식이 아닙니다: $trimmed" }
        return trimmed
    }

    /**
     * 양수 검증 (Int)
     */
    fun requirePositive(value: Int, fieldName: String): Int {
        require(value > 0) { "${fieldName}은(는) 양수여야 합니다. (입력값: $value)" }
        return value
    }

    /**
     * 양수 검증 (Long)
     */
    fun requirePositive(value: Long, fieldName: String): Long {
        require(value > 0) { "${fieldName}은(는) 양수여야 합니다. (입력값: $value)" }
        return value
    }

    /**
     * 양수 검증 (Double)
     */
    fun requirePositive(value: Double, fieldName: String): Double {
        require(value > 0) { "${fieldName}은(는) 양수여야 합니다. (입력값: $value)" }
        return value
    }

    /**
     * 0 이상인지 검증
     */
    fun requireNonNegative(value: Int, fieldName: String): Int {
        require(value >= 0) { "${fieldName}은(는) 0 이상이어야 합니다. (입력값: $value)" }
        return value
    }

    /**
     * 범위 검증 (min <= max)
     */
    fun validateRange(minValue: Double, maxValue: Double, minFieldName: String, maxFieldName: String) {
        require(minValue <= maxValue) {
            "$minFieldName($minValue)은(는) $maxFieldName($maxValue)보다 작거나 같아야 합니다."
        }
    }

    /**
     * 최대 길이 검증
     */
    fun validateMaxLength(value: String?, maxLength: Int, fieldName: String): String? {
        if (value == null) return null
        require(value.length <= maxLength) {
            "${fieldName}은(는) 최대 ${maxLength}자까지 입력 가능합니다. (현재: ${value.length}자)"
        }
        return value
    }

    /**
     * 검증 결과를 포함한 Result 반환
     */
    inline fun <T> validate(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }
}

/**
 * 검증 실패 시 반환할 표준 오류 응답
 */
data class ValidationError(
    val success: Boolean = false,
    val message: String,
    val field: String? = null
)
