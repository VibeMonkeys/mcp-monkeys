package com.monkeys.client.service

import com.monkeys.shared.dto.*
import com.monkeys.client.dto.MultiServiceResponse
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
    private val chatClient: ChatClient,
    private val conversationMemoryService: ConversationMemoryService,
    private val aiMetricsService: AiMetricsService
) {
    private val logger = LoggerFactory.getLogger(ChatService::class.java)
    
    companion object {
        private const val MODEL_NAME = "gemini-1.5-flash"
    }
    
    /**
     * 일반 채팅 응답 생성 (개선된 ChatClient API 사용 + 대화 메모리)
     */
    fun generateChatResponse(request: ChatRequest): ChatResponse {
        val sessionId = request.sessionId ?: "default-session"
        val requestType = "general_chat"
        logger.info("채팅 요청 처리: message=${maskMessage(request.message)}, sessionId=$sessionId")
        
        // 메트릭 시작
        val startTime = System.currentTimeMillis()
        val metricsSample = aiMetricsService.recordRequest(MODEL_NAME, requestType, sessionId)
        
        try {
            // 대화 컨텍스트 구성
            val conversationContext = conversationMemoryService.buildConversationContext(sessionId)
            val prompt = buildPrompt(request.message, conversationContext)
            
            // 컨텍스트 사용량 메트릭
            aiMetricsService.recordContextUsage(
                sessionId = sessionId,
                contextLength = conversationContext.length,
                contextMessages = conversationContext.split("사용자:").size - 1
            )
            
            val response = chatClient.prompt()
                .user(prompt)
                .system("당신은 MCP Monkeys의 통합 AI 어시스턴트입니다. 사용 가능한 도구들을 적절히 활용하여 사용자를 도와주세요. 이전 대화 내용을 참고하여 연속성 있는 대화를 진행하세요.")
                .call()
                .content() ?: "응답을 생성할 수 없습니다."
            
            logger.debug("응답 생성 완료: responseLength=${response.length}, sessionId=$sessionId")
            
            // 성공 메트릭 기록
            aiMetricsService.recordResponse(
                sample = metricsSample,
                modelName = MODEL_NAME,
                requestType = requestType,
                responseLength = response.length,
                success = true,
                tokenCount = estimateTokenCount(prompt + response)
            )
            
            // 대화 기록 저장 (비동기)
            conversationMemoryService.saveConversation(
                sessionId = sessionId,
                userMessage = request.message,
                aiResponse = response,
                requestType = requestType,
                responseTimeMs = System.currentTimeMillis() - startTime
            )
            
            return ChatResponse(
                response = response,
                sessionId = sessionId
            )
        } catch (e: Exception) {
            logger.error("채팅 응답 생성 실패: sessionId=$sessionId", e)
            
            // 에러 메트릭 기록
            aiMetricsService.recordError(
                modelName = MODEL_NAME,
                requestType = requestType,
                errorType = e::class.simpleName ?: "UnknownError",
                errorMessage = e.message ?: "Unknown error"
            )
            
            aiMetricsService.recordResponse(
                sample = metricsSample,
                modelName = MODEL_NAME,
                requestType = requestType,
                responseLength = 0,
                success = false
            )
            
            throw ChatServiceException("채팅 응답을 생성할 수 없습니다: ${e.message}", "CHAT_GENERATION_FAILED", e)
        }
    }
    
    /**
     * 구조화된 채팅 응답 생성 (개선된 .entity() 메서드 사용)
     */
    fun generateStructuredResponse(request: ChatRequest): StructuredChatResponse {
        val sessionId = request.sessionId ?: "default-session"
        logger.info("구조화된 채팅 요청 처리: message=${maskMessage(request.message)}, sessionId=$sessionId")
        
        try {
            val responseType = determineResponseType(request.message)
            
            logger.debug("응답 타입 결정: ${responseType.simpleName}, sessionId=$sessionId")
            
            val structuredResponse = chatClient.prompt()
                .user(request.message)
                .system("정확한 JSON 스키마를 따라 응답해주세요.")
                .call()
                .entity(responseType)
            
            // 기본 검증
            val validatedResponse = structuredResponse ?: "응답 생성 실패"
            
            logger.debug("구조화된 응답 생성 완료: type=${responseType.simpleName}, sessionId=$sessionId")
            
            return StructuredChatResponse(
                data = validatedResponse,
                format = "structured",
                sessionId = sessionId
            )
        } catch (e: Exception) {
            logger.error("구조화된 응답 생성 실패: sessionId=$sessionId", e)
            throw ChatServiceException("구조화된 응답을 생성할 수 없습니다: ${e.message}", "STRUCTURED_GENERATION_FAILED", e)
        }
    }
    
    /**
     * 스트리밍 채팅 응답 생성 (개선된 메타데이터 지원)
     */
    fun generateStreamingResponse(message: String, sessionId: String?): Flux<String> {
        val effectiveSessionId = sessionId ?: "default-session"
        logger.info("스트리밍 채팅 요청: message=${maskMessage(message)}, sessionId=$effectiveSessionId")
        
        return try {
            val prompt = buildPrompt(message)
            chatClient.prompt()
                .user(prompt)
                .system("스트리밍 응답을 위한 AI 어시스턴트입니다. 청크 단위로 자연스럽게 응답을 생성해주세요.")
                .stream()
                .content()
                .map { chunk -> "data: $chunk\n\n" }
                .onErrorReturn("data: [ERROR] 스트리밍 중 오류가 발생했습니다.\n\n")
        } catch (e: Exception) {
            logger.error("스트리밍 응답 생성 실패: sessionId=$effectiveSessionId", e)
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
     * 사용자 메시지를 AI용 프롬프트로 변환 (대화 컨텍스트 포함)
     */
    private fun buildPrompt(userMessage: String, conversationContext: String = ""): String {
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
     * 메시지 마스킹 (로깅용)
     */
    private fun maskMessage(message: String): String {
        return if (message.length > 100) "${message.take(97)}..." else message
    }
    
    /**
     * 토큰 수 추정 (대략적인 계산)
     */
    private fun estimateTokenCount(text: String): Int {
        // 일반적으로 영어는 단어당 1.3토큰, 한국어는 문자당 0.5토큰으로 추정
        val koreanChars = text.count { it.code > 127 }
        val otherChars = text.length - koreanChars
        return ((koreanChars * 0.5) + (otherChars * 0.25)).toInt()
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