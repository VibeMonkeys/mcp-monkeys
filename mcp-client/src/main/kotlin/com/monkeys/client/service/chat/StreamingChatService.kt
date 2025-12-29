package com.monkeys.client.service.chat

import com.monkeys.client.service.AiMetricsService
import com.monkeys.client.service.common.ChatResponseHelper
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

/**
 * 스트리밍 채팅 전용 서비스
 * 실시간 스트리밍 응답 생성에 특화됨
 */
@Service
class StreamingChatService(
    private val chatClient: ChatClient,
    private val aiMetricsService: AiMetricsService,
    private val chatResponseHelper: ChatResponseHelper
) {

    private val logger = LoggerFactory.getLogger(StreamingChatService::class.java)

    /**
     * 스트리밍 채팅 응답 생성 (Spring AI 2.0 ChatMemoryAdvisor 활용)
     */
    fun generateStreamingResponse(message: String, sessionId: String?): Flux<String> {
        val effectiveSessionId = chatResponseHelper.normalizeSessionId(sessionId)
        val requestType = "streaming_chat"

        logger.info("스트리밍 채팅 요청: message=${chatResponseHelper.maskMessage(message)}, sessionId=$effectiveSessionId")

        val metricsSample = aiMetricsService.recordRequest(
            ChatResponseHelper.MODEL_NAME,
            requestType,
            effectiveSessionId
        )

        return try {
            chatClient.prompt()
                .user(message)
                .advisors { it.param(ChatMemory.CONVERSATION_ID, effectiveSessionId) }
                .stream()
                .content()
                .map { chunk -> "data: $chunk\n\n" }
                .doOnComplete {
                    aiMetricsService.recordResponse(
                        sample = metricsSample,
                        modelName = ChatResponseHelper.MODEL_NAME,
                        requestType = requestType,
                        responseLength = 0,
                        success = true
                    )
                    logger.debug("스트리밍 응답 완료: sessionId=$effectiveSessionId")
                }
                .onErrorResume { error ->
                    logger.error("스트리밍 응답 생성 실패: sessionId=$effectiveSessionId", error)
                    recordError(metricsSample, requestType, error)
                    Flux.just("data: [ERROR] 스트리밍 중 오류가 발생했습니다: ${error.message}\n\n")
                }
        } catch (e: Exception) {
            logger.error("스트리밍 응답 시작 실패: sessionId=$effectiveSessionId", e)
            recordError(metricsSample, requestType, e)
            Flux.just("data: [ERROR] 스트리밍을 시작할 수 없습니다: ${e.message}\n\n")
        }
    }

    private fun recordError(metricsSample: io.micrometer.core.instrument.Timer.Sample, requestType: String, error: Throwable) {
        aiMetricsService.recordError(
            modelName = ChatResponseHelper.MODEL_NAME,
            requestType = requestType,
            errorType = error::class.simpleName ?: "UnknownError",
            errorMessage = error.message ?: "Unknown error"
        )
        aiMetricsService.recordResponse(
            sample = metricsSample,
            modelName = ChatResponseHelper.MODEL_NAME,
            requestType = requestType,
            responseLength = 0,
            success = false
        )
    }
}