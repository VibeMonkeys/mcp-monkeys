package com.monkeys.client.service.chat

import com.monkeys.client.service.AiMetricsService
import com.monkeys.client.service.common.ChatResponseHelper
import org.springframework.ai.chat.client.ChatClient
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
     * 스트리밍 채팅 응답 생성 (개선된 메타데이터 지원)
     */
    fun generateStreamingResponse(message: String, sessionId: String?): Flux<String> {
        val effectiveSessionId = chatResponseHelper.normalizeSessionId(sessionId)
        val requestType = "streaming_chat"
        
        logger.info("스트리밍 채팅 요청: message=${chatResponseHelper.maskMessage(message)}, sessionId=$effectiveSessionId")
        
        // 메트릭 시작
        val metricsSample = aiMetricsService.recordRequest(
            ChatResponseHelper.MODEL_NAME, 
            requestType, 
            effectiveSessionId
        )
        
        return try {
            val prompt = chatResponseHelper.buildPrompt(message)
            
            val streamFlux = chatClient.prompt()
                .user(prompt)
                .system(chatResponseHelper.getStreamingSystemMessage())
                .stream()
                .content()
                .map { chunk -> "data: $chunk\n\n" }
                .doOnComplete {
                    // 스트리밍 완료 메트릭 기록
                    aiMetricsService.recordResponse(
                        sample = metricsSample,
                        modelName = ChatResponseHelper.MODEL_NAME,
                        requestType = requestType,
                        responseLength = 0, // 스트리밍에서는 정확한 길이 측정 어려움
                        success = true
                    )
                    logger.debug("스트리밍 응답 완료: sessionId=$effectiveSessionId")
                }
                .onErrorResume { error ->
                    logger.error("스트리밍 응답 생성 실패: sessionId=$effectiveSessionId", error)
                    
                    // 에러 메트릭 기록
                    aiMetricsService.recordError(
                        modelName = ChatResponseHelper.MODEL_NAME,
                        requestType = requestType,
                        errorType = error::class.simpleName ?: "UnknownError",
                        errorMessage = error.message ?: "Unknown streaming error"
                    )
                    
                    aiMetricsService.recordResponse(
                        sample = metricsSample,
                        modelName = ChatResponseHelper.MODEL_NAME,
                        requestType = requestType,
                        responseLength = 0,
                        success = false
                    )
                    
                    Flux.just("data: [ERROR] 스트리밍 중 오류가 발생했습니다: ${error.message}\n\n")
                }
            
            streamFlux
            
        } catch (e: Exception) {
            logger.error("스트리밍 응답 시작 실패: sessionId=$effectiveSessionId", e)
            
            // 초기 에러 메트릭 기록
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
            
            Flux.just("data: [ERROR] 스트리밍을 시작할 수 없습니다: ${e.message}\n\n")
        }
    }
}