package com.monkeys.client.service

import io.micrometer.core.instrument.*
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * AI 전용 메트릭 수집 서비스
 */
@Service
class AiMetricsService(
    private val meterRegistry: MeterRegistry
) {
    
    // AI 요청 관련 메트릭
    private val aiRequestCounter = Counter.builder("ai.requests.total")
        .description("Total number of AI requests")
        .register(meterRegistry)
    
    private val aiResponseTimer = Timer.builder("ai.response.duration")
        .description("AI response time distribution")
        .register(meterRegistry)
    
    private val aiErrorCounter = Counter.builder("ai.errors.total")
        .description("Total number of AI errors")
        .register(meterRegistry)
    
    // 토큰 사용량 메트릭
    private val tokenUsageCounter = Counter.builder("ai.tokens.consumed")
        .description("Total tokens consumed by AI models")
        .register(meterRegistry)
    
    // 모델별 성능 메트릭
    private val modelPerformanceTimer = Timer.builder("ai.model.performance")
        .description("Performance metrics per AI model")
        .register(meterRegistry)
    
    // 요청 타입별 메트릭
    private val requestTypeCounter = Counter.builder("ai.requests.by_type")
        .description("AI requests grouped by type")
        .register(meterRegistry)
    
    // 세션 활동 메트릭은 별도로 관리
    
    // 대화 길이 메트릭
    private val conversationLengthHistogram = DistributionSummary.builder("ai.conversation.length")
        .description("Distribution of conversation lengths")
        .register(meterRegistry)
    
    // 캐시 적중률 메트릭은 별도로 관리
    
    /**
     * AI 요청 시작을 기록
     */
    fun recordRequest(
        modelName: String,
        requestType: String,
        sessionId: String
    ): Timer.Sample {
        Counter.builder("ai.requests.total")
            .description("Total number of AI requests")
            .tags("model", modelName, "request_type", requestType, "session_type", getSessionType(sessionId))
            .register(meterRegistry)
            .increment()
        
        Counter.builder("ai.requests.by_type")
            .description("AI requests grouped by type")
            .tags("type", requestType)
            .register(meterRegistry)
            .increment()
        
        return Timer.start(meterRegistry)
    }
    
    /**
     * AI 응답 완료를 기록
     */
    fun recordResponse(
        sample: Timer.Sample,
        modelName: String,
        requestType: String,
        responseLength: Int,
        success: Boolean,
        tokenCount: Int? = null
    ) {
        sample.stop(aiResponseTimer)
        sample.stop(modelPerformanceTimer)
        
        // 응답 길이 기록
        conversationLengthHistogram.record(responseLength.toDouble())
        
        // 토큰 사용량 기록
        tokenCount?.let {
            Counter.builder("ai.tokens.consumed")
                .description("Total tokens consumed by AI models")
                .tags("model", modelName)
                .register(meterRegistry)
                .increment(it.toDouble())
        }
        
        if (!success) {
            Counter.builder("ai.errors.total")
            .description("Total number of AI errors")
            .tags("model", modelName, "request_type", requestType, "success", success.toString())
            .register(meterRegistry)
            .increment()
        }
    }
    
    /**
     * 에러 발생을 기록
     */
    fun recordError(
        modelName: String,
        requestType: String,
        errorType: String,
        errorMessage: String
    ) {
        Counter.builder("ai.errors.total")
            .description("Total number of AI errors")
            .tags("model", modelName, "request_type", requestType, "error_type", errorType, "error_category", categorizeError(errorMessage))
            .register(meterRegistry)
            .increment()
    }
    
    /**
     * 구조화된 응답 품질을 기록
     */
    fun recordStructuredResponseQuality(
        modelName: String,
        responseType: String,
        parseSuccess: Boolean,
        validationScore: Double = 1.0
    ) {
        Counter.builder("ai.structured.responses")
            .description("Structured response success rate")
            .tags(
                "model", modelName,
                "response_type", responseType,
                "parse_success", parseSuccess.toString()
            )
            .register(meterRegistry)
            .increment()
        
        if (parseSuccess) {
            // Gauge metrics are complex to implement correctly with lambda suppliers
            // For now, we'll track this as a simple counter instead
            Counter.builder("ai.structured.quality_events")
                .description("Structured response quality events")
                .tags("model", modelName, "response_type", responseType)
                .register(meterRegistry)
                .increment()
        }
    }
    
    /**
     * 스트리밍 응답 메트릭 기록
     */
    fun recordStreamingMetrics(
        modelName: String,
        chunkCount: Int,
        streamDuration: Duration,
        avgChunkSize: Double
    ) {
        Counter.builder("ai.streaming.chunks")
            .description("Total streaming chunks sent")
            .tags("model", modelName)
            .register(meterRegistry)
            .increment(chunkCount.toDouble())
        
        Timer.builder("ai.streaming.duration")
            .description("Total streaming duration")
            .tags("model", modelName)
            .register(meterRegistry)
            .record(streamDuration)
        
        DistributionSummary.builder("ai.streaming.chunk_size")
            .description("Average streaming chunk size")
            .tags("model", modelName)
            .register(meterRegistry)
            .record(avgChunkSize)
    }
    
    /**
     * 도구 호출 메트릭 기록
     */
    fun recordToolCall(
        toolName: String,
        success: Boolean,
        executionTime: Duration
    ) {
        Counter.builder("ai.tools.calls")
            .description("AI tool calls")
            .tags(
                "tool", toolName,
                "success", success.toString()
            )
            .register(meterRegistry)
            .increment()
        
        Timer.builder("ai.tools.execution_time")
            .description("AI tool execution time")
            .tags("tool", toolName)
            .register(meterRegistry)
            .record(executionTime)
    }
    
    /**
     * 대화 컨텍스트 사용량 기록
     */
    fun recordContextUsage(
        sessionId: String,
        contextLength: Int,
        contextMessages: Int
    ) {
        DistributionSummary.builder("ai.context.length")
            .description("Context length in characters")
            .register(meterRegistry)
            .record(contextLength.toDouble())
        
        DistributionSummary.builder("ai.context.messages")
            .description("Number of messages in context")
            .register(meterRegistry)
            .record(contextMessages.toDouble())
    }
    
    // 헬퍼 메서드들
    private fun getSessionType(sessionId: String): String {
        return when {
            sessionId.startsWith("test-") -> "test"
            sessionId.startsWith("demo-") -> "demo"
            sessionId == "default-session" -> "default"
            else -> "user"
        }
    }
    
    private fun categorizeError(errorMessage: String): String {
        return when {
            errorMessage.contains("timeout", ignoreCase = true) -> "timeout"
            errorMessage.contains("rate limit", ignoreCase = true) -> "rate_limit"
            errorMessage.contains("authentication", ignoreCase = true) -> "auth"
            errorMessage.contains("quota", ignoreCase = true) -> "quota"
            errorMessage.contains("network", ignoreCase = true) -> "network"
            errorMessage.contains("parsing", ignoreCase = true) -> "parsing"
            else -> "unknown"
        }
    }
    
    private fun getActiveSessions(): Double {
        // 실제 구현에서는 ConversationMemoryService에서 활성 세션 수를 가져와야 함
        return 0.0
    }
    
    private fun getCacheHitRatio(): Double {
        // 향후 캐싱 구현 시 실제 캐시 적중률 반환
        return 0.0
    }
}