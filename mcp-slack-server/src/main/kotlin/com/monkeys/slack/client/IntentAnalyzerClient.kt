package com.monkeys.slack.client

import intent.IntentAnalyzerGrpcKt
import intent.Intent
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

/**
 * Intent Analyzer gRPC 클라이언트
 * Go로 작성된 Intent Analyzer 서버와 통신
 */
@Component
class IntentAnalyzerClient(
    @Value("\${intent-analyzer.host:localhost}") 
    private val host: String,
    @Value("\${intent-analyzer.port:8097}") 
    private val port: Int,
    @Value("\${intent-analyzer.timeout:5000}") 
    private val timeoutMs: Long
) {
    private val logger = LoggerFactory.getLogger(IntentAnalyzerClient::class.java)
    
    private val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext()
        .build()
    
    private val stub = IntentAnalyzerGrpcKt.IntentAnalyzerCoroutineStub(channel)
    
    /**
     * 의도 분석 요청
     */
    suspend fun analyzeIntent(
        text: String,
        domain: String = "slack",
        userId: String = "",
        contextMessages: List<String> = emptyList(),
        metadata: Map<String, String> = emptyMap()
    ): IntentAnalysisResult? {
        return try {
            logger.debug("Intent Analyzer 호출: text='$text', domain=$domain")
            
            val request = Intent.IntentRequest.newBuilder()
                .setText(text)
                .setDomain(domain)
                .setUserId(userId)
                .addAllContextMessages(contextMessages)
                .putAllMetadata(metadata)
                .build()
            
            val response = withTimeout(timeoutMs) {
                stub.analyzeIntent(request)
            }
            
            logger.debug("Intent Analyzer 응답: intent=${response.intentType}, confidence=${response.confidence}")
            
            IntentAnalysisResult(
                intentType = response.intentType,
                domainSpecificIntent = response.domainSpecificIntent,
                keywords = response.keywordsList.map { keyword ->
                    Keyword(keyword.text, keyword.weight, keyword.category) 
                },
                priority = mapProtobufPriority(response.priority),
                confidence = response.confidence,
                emotionalTone = mapProtobufEmotionalTone(response.emotionalTone),
                urgencyIndicators = emptyList(),
                reasoning = response.reasoning,
                intentScores = response.intentScoresMap,
                processingTimeMs = response.metrics.processingTimeMs
            )
            
        } catch (e: Exception) {
            logger.warn("Intent Analyzer 호출 실패: $text", e)
            null
        }
    }
    
    /**
     * 헬스체크
     */
    suspend fun isHealthy(): Boolean {
        return try {
            val request = Intent.HealthCheckRequest.newBuilder().build()
            val response = withTimeout(timeoutMs) {
                stub.healthCheck(request)
            }
            response.status == Intent.HealthCheckResponse.ServingStatus.SERVING
        } catch (e: Exception) {
            logger.warn("Intent Analyzer 헬스체크 실패", e)
            false
        }
    }
    
    @PreDestroy
    fun cleanup() {
        try {
            channel.shutdown()
            logger.info("Intent Analyzer gRPC 채널 종료")
        } catch (e: Exception) {
            logger.warn("gRPC 채널 종료 중 오류", e)
        }
    }
    
    /**
     * Protobuf Priority를 Kotlin enum으로 매핑
     */
    private fun mapProtobufPriority(protoPriority: Intent.Priority): Priority {
        return when (protoPriority) {
            Intent.Priority.PRIORITY_CRITICAL -> Priority.P0
            Intent.Priority.PRIORITY_URGENT -> Priority.P1
            Intent.Priority.PRIORITY_HIGH -> Priority.P2
            Intent.Priority.PRIORITY_MEDIUM -> Priority.P3
            Intent.Priority.PRIORITY_LOW -> Priority.P4
            else -> Priority.P3 // 기본값
        }
    }
    
    /**
     * Protobuf EmotionalTone을 Kotlin enum으로 매핑
     */
    private fun mapProtobufEmotionalTone(protoTone: Intent.EmotionalTone): EmotionalTone {
        return when (protoTone) {
            Intent.EmotionalTone.TONE_NEUTRAL -> EmotionalTone.NEUTRAL
            Intent.EmotionalTone.TONE_FRUSTRATED -> EmotionalTone.FRUSTRATED
            Intent.EmotionalTone.TONE_URGENT -> EmotionalTone.URGENT
            Intent.EmotionalTone.TONE_NEGATIVE -> EmotionalTone.CONFUSED
            Intent.EmotionalTone.TONE_POSITIVE -> EmotionalTone.SATISFIED
            Intent.EmotionalTone.TONE_GRATEFUL -> EmotionalTone.SATISFIED
            else -> EmotionalTone.NEUTRAL // 기본값
        }
    }
}

/**
 * Intent 분석 결과
 */
data class IntentAnalysisResult(
    val intentType: String,
    val domainSpecificIntent: String,
    val keywords: List<Keyword>,
    val priority: Priority,
    val confidence: Double,
    val emotionalTone: EmotionalTone,
    val urgencyIndicators: List<String>,
    val reasoning: String,
    val intentScores: Map<String, Double>,
    val processingTimeMs: Long
)

/**
 * 키워드 정보
 */
data class Keyword(
    val text: String,
    val weight: Double,
    val category: String
)

/**
 * 우선순위
 */
enum class Priority {
    P0, P1, P2, P3, P4
}

/**
 * 감정 톤
 */
enum class EmotionalTone {
    NEUTRAL, FRUSTRATED, URGENT, CONFUSED, SATISFIED
}