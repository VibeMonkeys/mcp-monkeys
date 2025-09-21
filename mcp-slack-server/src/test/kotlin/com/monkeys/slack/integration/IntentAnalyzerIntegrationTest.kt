package com.monkeys.slack.integration

import com.monkeys.slack.client.IntentAnalyzerClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.slf4j.LoggerFactory

/**
 * Intent Analyzer와 Slack MCP 서버의 통합 테스트
 */
class IntentAnalyzerIntegrationTest {
    
    private val logger = LoggerFactory.getLogger(IntentAnalyzerIntegrationTest::class.java)
    
    @Test
    fun `Intent Analyzer 헬스체크 테스트`() = runBlocking {
        val client = IntentAnalyzerClient(
            host = "localhost",
            port = 8097,
            timeoutMs = 5000
        )
        
        val isHealthy = client.isHealthy()
        logger.info("Intent Analyzer 헬스체크 결과: $isHealthy")
        
        assertTrue(isHealthy, "Intent Analyzer 서버가 응답하지 않습니다")
    }
    
    @Test
    fun `한국어 질문 의도 분석 테스트`() = runBlocking {
        val client = IntentAnalyzerClient(
            host = "localhost", 
            port = 8097,
            timeoutMs = 5000
        )
        
        val testQuestions = listOf(
            "어떻게 배포하나요?",
            "이거 왜 안돼?",
            "도와주세요!",
            "서버 상태 알려주세요"
        )
        
        for (question in testQuestions) {
            logger.info("테스트 질문: $question")
            
            val result = client.analyzeIntent(
                text = question,
                domain = "slack"
            )
            
            assertNotNull(result, "Intent 분석 결과가 null입니다: $question")
            result?.let {
                logger.info("결과: intent=${it.intentType}, confidence=${it.confidence}, priority=${it.priority}")
                
                assertTrue(it.confidence > 0.0, "신뢰도가 0보다 커야 합니다")
                assertTrue(it.intentType.isNotBlank(), "Intent type이 비어있으면 안됩니다")
                assertTrue(it.processingTimeMs > 0, "처리 시간이 0보다 커야 합니다")
            }
        }
    }
    
    @Test
    fun `유사도 계산 성능 테스트`() = runBlocking {
        val client = IntentAnalyzerClient(
            host = "localhost",
            port = 8097, 
            timeoutMs = 5000
        )
        
        val question1 = "배포 방법 알려주세요"
        val question2 = "어떻게 배포하나요?"
        
        val startTime = System.currentTimeMillis()
        
        val intent1 = client.analyzeIntent(question1, "slack")
        val intent2 = client.analyzeIntent(question2, "slack")
        
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        
        logger.info("두 질문 분석 총 시간: ${totalTime}ms")
        
        assertNotNull(intent1)
        assertNotNull(intent2)
        
        if (intent1 != null && intent2 != null) {
            logger.info("질문1: intent=${intent1.intentType}, keywords=${intent1.keywords.map { it.text }}")
            logger.info("질문2: intent=${intent2.intentType}, keywords=${intent2.keywords.map { it.text }}")
            
            // 유사한 질문이므로 같은 intent type을 가져야 함
            assertEquals(intent1.intentType, intent2.intentType, "유사한 질문들은 같은 intent type을 가져야 합니다")
        }
        
        // 성능 체크: 총 5초 이내에 완료되어야 함 (네트워크 지연 고려)
        assertTrue(totalTime < 5000, "Intent 분석이 너무 오래 걸립니다: ${totalTime}ms")
    }
}