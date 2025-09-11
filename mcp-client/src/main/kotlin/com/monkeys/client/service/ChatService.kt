package com.monkeys.client.service

import com.monkeys.shared.dto.*
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

/**
 * 채팅 비즈니스 로직 서비스
 * AI 채팅 관련 핵심 비즈니스 로직만 담당
 */
@Service
class ChatService(
    private val chatClient: ChatClient
) {
    private val logger = LoggerFactory.getLogger(ChatService::class.java)
    
    /**
     * 일반 채팅 응답 생성
     */
    fun generateChatResponse(request: ChatRequest): ChatResponse {
        logger.info("채팅 요청 처리: message=${maskMessage(request.message)}, sessionId=${request.sessionId}")
        
        try {
            val prompt = buildPrompt(request.message)
            val response = chatClient.prompt()
                .user(prompt)
                .call()
                .content() ?: "응답을 생성할 수 없습니다."
            
            return ChatResponse(
                response = response,
                sessionId = request.sessionId ?: "default-session"
            )
        } catch (e: Exception) {
            logger.error("채팅 응답 생성 실패", e)
            throw ChatServiceException("채팅 응답을 생성할 수 없습니다: ${e.message}", "CHAT_GENERATION_FAILED", e)
        }
    }
    
    /**
     * 구조화된 채팅 응답 생성
     */
    fun generateStructuredResponse(request: ChatRequest): StructuredChatResponse {
        logger.info("구조화된 채팅 요청 처리: message=${maskMessage(request.message)}")
        
        try {
            val responseType = determineResponseType(request.message)
            val converter = BeanOutputConverter(responseType)
            
            val structuredResponse = chatClient.prompt()
                .user("${request.message}\n\n${converter.format}")
                .call()
                .entity(responseType)
            
            return StructuredChatResponse(
                data = structuredResponse ?: "No response",
                format = "structured",
                sessionId = request.sessionId ?: "default-session"
            )
        } catch (e: Exception) {
            logger.error("구조화된 응답 생성 실패", e)
            throw ChatServiceException("구조화된 응답을 생성할 수 없습니다: ${e.message}", "STRUCTURED_GENERATION_FAILED", e)
        }
    }
    
    /**
     * 스트리밍 채팅 응답 생성
     */
    fun generateStreamingResponse(message: String, sessionId: String?): Flux<String> {
        logger.info("스트리밍 채팅 요청: message=${maskMessage(message)}, sessionId=$sessionId")
        
        return try {
            val prompt = buildPrompt(message)
            chatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .map { chunk -> "data: $chunk\n\n" }
                .onErrorReturn("data: [ERROR] 스트리밍 중 오류가 발생했습니다.\n\n")
        } catch (e: Exception) {
            logger.error("스트리밍 응답 생성 실패", e)
            Flux.just("data: [ERROR] 스트리밍을 시작할 수 없습니다.\n\n")
        }
    }
    
    /**
     * 메시지 타입에 따른 응답 타입 결정
     */
    private fun determineResponseType(message: String): Class<*> {
        return when {
            message.contains("날씨", ignoreCase = true) || 
            message.contains("weather", ignoreCase = true) -> WeatherResponse::class.java
            
            message.contains("뉴스", ignoreCase = true) || 
            message.contains("news", ignoreCase = true) -> NewsResponse::class.java
            
            message.contains("번역", ignoreCase = true) || 
            message.contains("translate", ignoreCase = true) -> TranslationResponse::class.java
            
            message.contains("일정", ignoreCase = true) || 
            message.contains("calendar", ignoreCase = true) -> CalendarResponse::class.java
            
            else -> MultiServiceResponse::class.java
        }
    }
    
    /**
     * 사용자 메시지를 AI용 프롬프트로 변환
     */
    private fun buildPrompt(userMessage: String): String {
        return """
사용자 요청: $userMessage

요청을 분석하여 적절한 도구를 사용하고 도움이 되는 정보를 제공해주세요.
여러 시스템을 연계해야 하는 경우 순서대로 처리해주세요.
        """.trimIndent()
    }
    
    /**
     * 메시지 마스킹 (로깅용)
     */
    private fun maskMessage(message: String): String {
        return if (message.length > 100) "${message.take(97)}..." else message
    }
}

/**
 * Chat Service 전용 예외
 */
class ChatServiceException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)