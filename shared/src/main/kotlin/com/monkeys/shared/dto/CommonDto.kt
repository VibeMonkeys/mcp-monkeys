package com.monkeys.shared.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import java.time.LocalDateTime

/**
 * 공통 기본 응답 구조
 */
data class BaseResponse<T>(
    @JsonPropertyDescription("응답 데이터")
    val data: T?,
    
    @JsonPropertyDescription("성공 여부")
    val success: Boolean,
    
    @JsonPropertyDescription("메시지")
    val message: String?,
    
    @JsonPropertyDescription("타임스탬프")
    val timestamp: Long = System.currentTimeMillis(),
    
    @JsonPropertyDescription("에러 정보")
    val error: ErrorInfo? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): BaseResponse<T> {
            return BaseResponse(data, true, message, System.currentTimeMillis(), null)
        }
        
        fun <T> failure(message: String, errorCode: String? = null): BaseResponse<T> {
            return BaseResponse(
                null, 
                false, 
                message, 
                System.currentTimeMillis(), 
                ErrorInfo(errorCode ?: "UNKNOWN_ERROR", message)
            )
        }
    }
}

/**
 * 에러 정보 DTO
 */
data class ErrorInfo(
    @JsonPropertyDescription("에러 코드")
    val code: String,
    
    @JsonPropertyDescription("에러 메시지") 
    val message: String,
    
    @JsonPropertyDescription("상세 정보")
    val details: Map<String, Any>? = null
)

/**
 * 페이징 정보 DTO
 */
data class PageInfo(
    @JsonPropertyDescription("현재 페이지")
    val page: Int,
    
    @JsonPropertyDescription("페이지 크기")
    val size: Int,
    
    @JsonPropertyDescription("전체 항목 수")
    val totalElements: Long,
    
    @JsonPropertyDescription("전체 페이지 수") 
    val totalPages: Int,
    
    @JsonPropertyDescription("첫 페이지 여부")
    val isFirst: Boolean,
    
    @JsonPropertyDescription("마지막 페이지 여부")
    val isLast: Boolean
)

/**
 * 페이징된 응답 DTO
 */
data class PagedResponse<T>(
    @JsonPropertyDescription("데이터 목록")
    val content: List<T>,
    
    @JsonPropertyDescription("페이징 정보")
    val pageInfo: PageInfo
)

/**
 * API 상태 결과 DTO
 */
data class ApiStatusResult(
    @JsonPropertyDescription("GitHub 상태")
    val github: String,
    
    @JsonPropertyDescription("Jira 상태")
    val jira: String,
    
    @JsonPropertyDescription("Gmail 상태")
    val gmail: String,
    
    @JsonPropertyDescription("Slack 상태")
    val slack: String,
    
    @JsonPropertyDescription("확인 시각")
    val timestamp: Long
)

/**
 * 채팅 요청 DTO
 */
data class ChatRequest(
    @JsonPropertyDescription("사용자 메시지")
    val message: String,
    
    @JsonPropertyDescription("세션 ID")
    val sessionId: String? = null,
    
    @JsonPropertyDescription("응답 형식 (text, structured)")
    val format: String? = "text",
    
    @JsonPropertyDescription("요청 옵션")
    val options: Map<String, Any>? = null
)

/**
 * 채팅 응답 DTO
 */
data class ChatResponse(
    @JsonPropertyDescription("AI 응답")
    val response: String,
    
    @JsonPropertyDescription("세션 ID") 
    val sessionId: String?,
    
    @JsonPropertyDescription("사용된 도구 목록")
    val usedTools: List<String>? = null,
    
    @JsonPropertyDescription("응답 시각")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 구조화된 채팅 응답 DTO
 */
data class StructuredChatResponse(
    @JsonPropertyDescription("구조화된 데이터")
    val data: Any,
    
    @JsonPropertyDescription("응답 형식")
    val format: String,
    
    @JsonPropertyDescription("세션 ID")
    val sessionId: String?,
    
    @JsonPropertyDescription("메타데이터")
    val metadata: Map<String, Any>? = null,
    
    @JsonPropertyDescription("응답 시각")
    val timestamp: Long = System.currentTimeMillis()
)