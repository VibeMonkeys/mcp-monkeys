package com.monkeys.client.service.common

import com.monkeys.client.dto.*
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

/**
 * 채팅 관련 공통 기능을 제공하는 헬퍼 클래스
 * 모든 채팅 서비스에서 공통으로 사용하는 기능들을 중앙화
 */
@Component
class ChatResponseHelper {
    
    private val logger = LoggerFactory.getLogger(ChatResponseHelper::class.java)
    
    companion object {
        const val MODEL_NAME = "gemini-1.5-flash"
        const val DEFAULT_SESSION_ID = "default-session"
    }
    
    /**
     * 사용자 메시지를 AI용 프롬프트로 변환 (대화 컨텍스트 포함)
     */
    fun buildPrompt(userMessage: String, conversationContext: String = ""): String {
        return if (conversationContext.isNotEmpty()) {
            """
$conversationContext

현재 사용자 요청: $userMessage

이전 대화 내용을 참고하여 요청을 분석하고 적절한 도구를 사용하여 도움이 되는 정보를 제공해주세요.
여러 시스템을 연계해야 하는 경우 순서대로 처리해주세요.
대화의 연속성을 유지하며 자연스럽게 응답해주세요.
            """.trimIndent()
        } else {
            """
사용자 요청: $userMessage

요청을 분석하여 적절한 도구를 사용하고 도움이 되는 정보를 제공해주세요.
여러 시스템을 연계해야 하는 경우 순서대로 처리해주세요.
            """.trimIndent()
        }
    }

    /**
     * 구조화된 응답을 위한 프롬프트 구성
     * 모델이 BeanOutputConverter 포맷을 따르도록 구체적인 지시를 포함
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
        // 일반적으로 영어는 단어당 1.3토큰, 한국어는 문자당 0.5토큰으로 추정
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
     * 기본 시스템 메시지 생성
     */
    fun getBasicSystemMessage(): String {
        return "당신은 MCP Monkeys의 통합 AI 어시스턴트입니다. 사용 가능한 도구들을 적절히 활용하여 사용자를 도와주세요. 이전 대화 내용을 참고하여 연속성 있는 대화를 진행하세요."
    }
    
    /**
     * 구조화된 응답용 시스템 메시지 생성
     */
    fun getStructuredSystemMessage(): String {
        return "정확한 JSON 스키마를 따라 응답해주세요."
    }
    
    /**
     * 스트리밍용 시스템 메시지 생성
     */
    fun getStreamingSystemMessage(): String {
        return "스트리밍 응답을 위한 AI 어시스턴트입니다. 청크 단위로 자연스럽게 응답을 생성해주세요."
    }
    
    /**
     * 요청 메타데이터 생성
     */
    fun buildRequestMetadata(sessionId: String, requestType: String): Map<String, Any> {
        return mapOf(
            "sessionId" to sessionId,
            "requestType" to requestType,
            "requestTime" to System.currentTimeMillis()
        )
    }
}
