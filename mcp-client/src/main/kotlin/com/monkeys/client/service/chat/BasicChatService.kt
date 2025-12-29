package com.monkeys.client.service.chat

import com.monkeys.shared.dto.ChatRequest
import com.monkeys.shared.dto.ChatResponse
import com.monkeys.client.service.AiMetricsService
import com.monkeys.client.service.common.ChatResponseHelper
import com.monkeys.client.exception.ChatServiceException
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * 일반 채팅 전용 서비스
 * Spring AI 2.0 ChatMemoryAdvisor를 통한 자동 대화 메모리 관리
 */
@Service
class BasicChatService(
    private val chatClient: ChatClient,
    private val aiMetricsService: AiMetricsService,
    private val chatResponseHelper: ChatResponseHelper
) {

    private val logger = LoggerFactory.getLogger(BasicChatService::class.java)

    /**
     * 일반 채팅 응답 생성 (Spring AI 2.0 ChatMemoryAdvisor 활용)
     */
    fun generateResponse(request: ChatRequest): ChatResponse {
        val sessionId = chatResponseHelper.normalizeSessionId(request.sessionId)
        val requestType = "general_chat"

        logger.info("채팅 요청 처리: message=${chatResponseHelper.maskMessage(request.message)}, sessionId=$sessionId")

        val startTime = System.currentTimeMillis()
        val metricsSample = aiMetricsService.recordRequest(
            ChatResponseHelper.MODEL_NAME,
            requestType,
            sessionId
        )

        try {
            // ChatMemoryAdvisor가 자동으로 대화 컨텍스트를 관리
            // advisorParams로 세션 ID 전달
            val chatResponse = chatClient.prompt()
                .user(request.message)
                .advisors { advisorSpec ->
                    advisorSpec.param(ChatMemory.CONVERSATION_ID, sessionId)
                }
                .call()

            val response = chatResponse.content() ?: "응답을 생성할 수 없습니다."

            logger.debug("응답 생성 완료: responseLength=${response.length}, sessionId=$sessionId")

            val estimatedTokens = chatResponseHelper.estimateTokenCount(request.message + response)
            aiMetricsService.recordResponse(
                sample = metricsSample,
                modelName = ChatResponseHelper.MODEL_NAME,
                requestType = requestType,
                responseLength = response.length,
                success = true,
                tokenCount = estimatedTokens
            )

            return ChatResponse(
                response = response,
                sessionId = sessionId
            )

        } catch (e: Exception) {
            logger.error("채팅 응답 생성 실패: sessionId=$sessionId", e)

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
