package com.monkeys.client.service.chat

import com.monkeys.shared.dto.ChatRequest
import com.monkeys.shared.dto.StructuredChatResponse
import com.monkeys.client.service.AiMetricsService
import com.monkeys.client.service.common.ChatResponseHelper
import com.monkeys.client.service.ChatServiceException
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * 구조화된 응답 전용 서비스
 * 타입 안전성과 구조화된 데이터 파싱에 특화됨
 */
@Service
class StructuredResponseService(
    private val chatClient: ChatClient,
    private val aiMetricsService: AiMetricsService,
    private val chatResponseHelper: ChatResponseHelper
) {
    
    private val logger = LoggerFactory.getLogger(StructuredResponseService::class.java)
    
    /**
     * 구조화된 채팅 응답 생성 (개선된 .entity() 메서드 사용)
     */
    fun generateStructuredResponse(request: ChatRequest): StructuredChatResponse {
        val sessionId = chatResponseHelper.normalizeSessionId(request.sessionId)
        val requestType = "structured_response"
        
        logger.info("구조화된 채팅 요청 처리: message=${chatResponseHelper.maskMessage(request.message)}, sessionId=$sessionId")
        
        // 메트릭 시작
        val metricsSample = aiMetricsService.recordRequest(
            ChatResponseHelper.MODEL_NAME, 
            requestType, 
            sessionId
        )
        
        try {
            val responseType = chatResponseHelper.determineResponseType(request.message)
            
            logger.debug("응답 타입 결정: ${responseType.simpleName}, sessionId=$sessionId")
            
            val structuredResponse = chatClient.prompt()
                .user(request.message)
                .system(chatResponseHelper.getStructuredSystemMessage())
                .call()
                .entity(responseType)
            
            // 기본 검증
            val validatedResponse = structuredResponse ?: "응답 생성 실패"
            
            logger.debug("구조화된 응답 생성 완료: type=${responseType.simpleName}, sessionId=$sessionId")
            
            // 성공 메트릭 기록
            val estimatedTokens = chatResponseHelper.estimateTokenCount(request.message + validatedResponse.toString())
            aiMetricsService.recordResponse(
                sample = metricsSample,
                modelName = ChatResponseHelper.MODEL_NAME,
                requestType = requestType,
                responseLength = validatedResponse.toString().length,
                success = true,
                tokenCount = estimatedTokens
            )
            
            return StructuredChatResponse(
                data = validatedResponse,
                format = "structured",
                sessionId = sessionId
            )
            
        } catch (e: Exception) {
            logger.error("구조화된 응답 생성 실패: sessionId=$sessionId", e)
            
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
            
            throw ChatServiceException("구조화된 응답을 생성할 수 없습니다: ${e.message}", "STRUCTURED_GENERATION_FAILED", e)
        }
    }
}