package com.monkeys.client.service

import com.monkeys.client.entity.ConversationHistory
import com.monkeys.client.entity.ConversationStats
import com.monkeys.client.repository.ConversationHistoryRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * 대화 메모리 및 컨텍스트 관리 서비스
 */
@Service
@Transactional
class ConversationMemoryService(
    private val conversationHistoryRepository: ConversationHistoryRepository
) {
    private val logger = LoggerFactory.getLogger(ConversationMemoryService::class.java)
    
    /**
     * 대화 기록 저장
     */
    @Async
    fun saveConversation(
        sessionId: String,
        userMessage: String,
        aiResponse: String,
        requestType: String = "general_chat",
        responseTimeMs: Long? = null
    ) {
        try {
            val conversation = ConversationHistory(
                sessionId = sessionId,
                userMessage = userMessage,
                aiResponse = aiResponse,
                requestType = requestType,
                responseTimeMs = responseTimeMs,
                createdAt = LocalDateTime.now()
            )
            
            conversationHistoryRepository.save(conversation)
            logger.debug("대화 기록 저장 완료: sessionId=$sessionId, requestType=$requestType")
        } catch (e: Exception) {
            logger.error("대화 기록 저장 실패: sessionId=$sessionId", e)
        }
    }
    
    /**
     * 세션의 최근 대화 기록 조회
     */
    fun getRecentConversations(sessionId: String, limit: Int = 10): List<ConversationHistory> {
        return try {
            if (limit <= 10) {
                conversationHistoryRepository.findTop10BySessionIdOrderByCreatedAtDesc(sessionId)
            } else {
                conversationHistoryRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
                    .take(limit)
            }
        } catch (e: Exception) {
            logger.error("대화 기록 조회 실패: sessionId=$sessionId", e)
            emptyList()
        }
    }
    
    /**
     * 세션의 컨텍스트를 위한 요약된 대화 내용 생성
     */
    fun buildConversationContext(sessionId: String, maxMessages: Int = 5): String {
        return try {
            val recentConversations = getRecentConversations(sessionId, maxMessages)
            
            if (recentConversations.isEmpty()) {
                return ""
            }
            
            val context = StringBuilder()
            context.append("이전 대화 내용:\n")
            
            recentConversations.reversed().forEach { conversation ->
                context.append("사용자: ${conversation.userMessage}\n")
                context.append("AI: ${conversation.aiResponse}\n\n")
            }
            
            context.toString()
        } catch (e: Exception) {
            logger.error("대화 컨텍스트 생성 실패: sessionId=$sessionId", e)
            ""
        }
    }
    
    /**
     * 세션 통계 정보 조회
     */
    fun getConversationStats(sessionId: String): ConversationStats? {
        return try {
            val totalConversations = conversationHistoryRepository.countBySessionId(sessionId)
            
            if (totalConversations == 0L) {
                return null
            }
            
            val averageResponseTime = conversationHistoryRepository.findAverageResponseTimeBySessionId(sessionId) ?: 0.0
            val firstConversationAt = conversationHistoryRepository.findFirstConversationTimeBySessionId(sessionId)
            val lastConversationAt = conversationHistoryRepository.findLastConversationTimeBySessionId(sessionId)
            val mostCommonRequestType = conversationHistoryRepository.findMostCommonRequestTypeBySessionId(sessionId) ?: "general_chat"
            
            ConversationStats(
                sessionId = sessionId,
                totalConversations = totalConversations,
                averageResponseTime = averageResponseTime,
                firstConversationAt = firstConversationAt ?: LocalDateTime.now(),
                lastConversationAt = lastConversationAt ?: LocalDateTime.now(),
                mostCommonRequestType = mostCommonRequestType
            )
        } catch (e: Exception) {
            logger.error("대화 통계 조회 실패: sessionId=$sessionId", e)
            null
        }
    }
    
    /**
     * 특정 기간 내 대화 기록 조회
     */
    fun getConversationsByTimeRange(
        sessionId: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<ConversationHistory> {
        return try {
            conversationHistoryRepository.findBySessionIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                sessionId, startTime, endTime
            )
        } catch (e: Exception) {
            logger.error("기간별 대화 기록 조회 실패: sessionId=$sessionId", e)
            emptyList()
        }
    }
    
    /**
     * 활성 세션 목록 조회 (최근 24시간 내 대화가 있는)
     */
    fun getActiveSessionIds(): List<String> {
        return try {
            val cutoffTime = LocalDateTime.now().minusHours(24)
            conversationHistoryRepository.findActiveSessionIds(cutoffTime)
        } catch (e: Exception) {
            logger.error("활성 세션 조회 실패", e)
            emptyList()
        }
    }
    
    /**
     * 오래된 대화 기록 정리 (30일 이상 된 기록 삭제)
     * 매일 새벽 2시에 실행
     */
    @Scheduled(cron = "0 0 2 * * ?")
    fun cleanupOldConversations() {
        try {
            val cutoffTime = LocalDateTime.now().minusDays(30)
            val deletedCount = conversationHistoryRepository.deleteByCreatedAtBefore(cutoffTime)
            
            if (deletedCount > 0) {
                logger.info("오래된 대화 기록 정리 완료: ${deletedCount}개 삭제")
            }
        } catch (e: Exception) {
            logger.error("오래된 대화 기록 정리 실패", e)
        }
    }
    
    /**
     * 세션의 대화 패턴 분석
     */
    fun analyzeConversationPattern(sessionId: String): Map<String, Any> {
        return try {
            val stats = getConversationStats(sessionId) ?: return emptyMap()
            val recentConversations = getRecentConversations(sessionId, 20)
            
            val requestTypeDistribution = recentConversations
                .groupingBy { it.requestType }
                .eachCount()
            
            val averageMessageLength = recentConversations
                .map { it.userMessage.length }
                .average()
            
            val conversationFrequency = if (recentConversations.size >= 2) {
                val timeSpan = recentConversations.first().createdAt
                    .until(recentConversations.last().createdAt, java.time.temporal.ChronoUnit.HOURS)
                if (timeSpan > 0) recentConversations.size.toDouble() / timeSpan else 0.0
            } else 0.0
            
            mapOf(
                "sessionId" to sessionId,
                "totalConversations" to stats.totalConversations,
                "averageResponseTime" to stats.averageResponseTime,
                "requestTypeDistribution" to requestTypeDistribution,
                "averageMessageLength" to averageMessageLength,
                "conversationFrequency" to conversationFrequency,
                "mostActiveRequestType" to stats.mostCommonRequestType
            )
        } catch (e: Exception) {
            logger.error("대화 패턴 분석 실패: sessionId=$sessionId", e)
            emptyMap()
        }
    }
}