package com.monkeys.client.service.common

import com.monkeys.client.dto.*
import org.springframework.stereotype.Component

/**
 * 채팅 관련 공통 기능을 제공하는 헬퍼 클래스
 */
@Component
class ChatResponseHelper {

    companion object {
        const val MODEL_NAME = "gemini-1.5-flash"
        const val DEFAULT_SESSION_ID = "default-session"
    }

    /**
     * 구조화된 응답을 위한 프롬프트 구성
     */
    fun buildStructuredPrompt(userMessage: String, schemaFormat: String): String {
        return """
사용자 요청: $userMessage

위 요청에 답변할 때 아래 JSON 스키마를 반드시 따르세요.
모든 필드는 지정된 타입에 맞춰 응답하고, 필요한 경우 예시 값을 참고하세요.

$schemaFormat
        """.trimIndent()
    }

    /**
     * 토큰 수 추정 (대략적인 계산)
     */
    fun estimateTokenCount(text: String): Int {
        val koreanChars = text.count { it.code > 127 }
        val otherChars = text.length - koreanChars
        return ((koreanChars * 0.5) + (otherChars * 0.25)).toInt()
    }

    /**
     * 메시지 마스킹 (로깅용)
     */
    fun maskMessage(message: String): String {
        return if (message.length > 100) "${message.take(97)}..." else message
    }

    /**
     * 메시지 타입에 따른 응답 타입 결정
     */
    fun determineResponseType(message: String): Class<*> {
        return when {
            message.contains("도서", ignoreCase = true) ||
            message.contains("책", ignoreCase = true) ||
            message.contains("대출", ignoreCase = true) ||
            message.contains("library", ignoreCase = true) ||
            message.contains("book", ignoreCase = true) -> LibraryResponse::class.java

            message.contains("할일", ignoreCase = true) ||
            message.contains("todo", ignoreCase = true) ||
            message.contains("task", ignoreCase = true) -> TodoResponse::class.java

            message.contains("직원", ignoreCase = true) ||
            message.contains("사원", ignoreCase = true) ||
            message.contains("부서", ignoreCase = true) ||
            message.contains("employee", ignoreCase = true) -> EmployeeResponse::class.java

            message.contains("상품", ignoreCase = true) ||
            message.contains("재고", ignoreCase = true) ||
            message.contains("product", ignoreCase = true) ||
            message.contains("inventory", ignoreCase = true) -> ProductResponse::class.java

            else -> MultiServiceResponse::class.java
        }
    }

    /**
     * 세션 ID 정규화
     */
    fun normalizeSessionId(sessionId: String?): String {
        return sessionId?.takeIf { it.isNotBlank() } ?: DEFAULT_SESSION_ID
    }

    /**
     * 구조화된 응답용 시스템 메시지
     */
    fun getStructuredSystemMessage(): String {
        return "정확한 JSON 스키마를 따라 응답해주세요."
    }
}
