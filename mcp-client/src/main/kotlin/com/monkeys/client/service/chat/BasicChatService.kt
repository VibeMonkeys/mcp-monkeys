package com.monkeys.client.service.chat

import com.monkeys.shared.dto.ChatRequest
import com.monkeys.shared.dto.ChatResponse
import com.monkeys.client.service.AiMetricsService
import com.monkeys.client.service.ConversationMemoryService
import com.monkeys.client.service.common.ChatResponseHelper
import com.monkeys.client.service.ChatServiceException
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * 일반 채팅 전용 서비스
 * 대화 메모리와 컨텍스트 관리에 특화됨
 */
@Service
class BasicChatService(
    private val chatClient: ChatClient,
    private val conversationMemoryService: ConversationMemoryService,
    private val aiMetricsService: AiMetricsService,
    private val chatResponseHelper: ChatResponseHelper
) {
    
    private val logger = LoggerFactory.getLogger(BasicChatService::class.java)
    
    /**
     * 일반 채팅 응답 생성 (개선된 ChatClient API 사용 + 대화 메모리)
     */
    fun generateResponse(request: ChatRequest): ChatResponse {
        val sessionId = chatResponseHelper.normalizeSessionId(request.sessionId)
        val requestType = "general_chat"
        
        logger.info("채팅 요청 처리: message=${chatResponseHelper.maskMessage(request.message)}, sessionId=$sessionId")
        
        // 메트릭 시작
        val startTime = System.currentTimeMillis()
        val metricsSample = aiMetricsService.recordRequest(
            ChatResponseHelper.MODEL_NAME, 
            requestType, 
            sessionId
        )
        
        try {
            // 대화 컨텍스트 구성
            val conversationContext = conversationMemoryService.buildConversationContext(sessionId)
            val prompt = chatResponseHelper.buildPrompt(request.message, conversationContext)
            
            // 컨텍스트 사용량 메트릭
            aiMetricsService.recordContextUsage(
                sessionId = sessionId,
                contextLength = conversationContext.length,
                contextMessages = conversationContext.split("사용자:").size - 1
            )
            
            val chatResponse = chatClient.prompt()
                .user(prompt)
                .system(chatResponseHelper.getBasicSystemMessage())
                .call()

            val response = chatResponse.content() ?: "응답을 생성할 수 없습니다."
            
            logger.debug("응답 생성 완료: responseLength=${response.length}, sessionId=$sessionId")
            
            // 성공 메트릭 기록
            val estimatedTokens = chatResponseHelper.estimateTokenCount(prompt + response)
            aiMetricsService.recordResponse(
                sample = metricsSample,
                modelName = ChatResponseHelper.MODEL_NAME,
                requestType = requestType,
                responseLength = response.length,
                success = true,
                tokenCount = estimatedTokens
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
                modelName = ChatResponseHelper.MODEL_NAME,
                requestType = requestType,
                errorType = e::class.simpleName ?: "UnknownError",
                errorMessage = e.message ?: "Unknown error"
            )
            
            aiMetricsService.recordResponse(
                sample = metricsSample,
                modelName = ChatResponseHelper.MODEL_NAME,
                requestType = requestType,
                responseLength = 0,
                success = false
            )
            
            throw ChatServiceException("채팅 응답을 생성할 수 없습니다: ${e.message}", "CHAT_GENERATION_FAILED", e)
        }
    }
}
