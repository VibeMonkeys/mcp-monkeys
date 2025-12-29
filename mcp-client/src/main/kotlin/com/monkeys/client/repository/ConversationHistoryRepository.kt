package com.monkeys.client.repository

import com.monkeys.client.entity.ConversationHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ConversationHistoryRepository : JpaRepository<ConversationHistory, String> {
    
    /**
     * 세션 ID로 대화 기록 조회 (최신순)
     */
    fun findBySessionIdOrderByCreatedAtDesc(sessionId: String): List<ConversationHistory>
    
    /**
     * 세션 ID로 최근 N개 대화 기록 조회
     */
    fun findTop10BySessionIdOrderByCreatedAtDesc(sessionId: String): List<ConversationHistory>
    
    /**
     * 세션 ID로 특정 기간 내 대화 기록 조회
     */
    fun findBySessionIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        sessionId: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<ConversationHistory>
    
    /**
     * 세션 ID로 대화 개수 조회
     */
    fun countBySessionId(sessionId: String): Long
    
    /**
     * 세션 ID로 평균 응답 시간 조회
     */
    @Query("SELECT AVG(ch.responseTimeMs) FROM ConversationHistory ch WHERE ch.sessionId = :sessionId AND ch.responseTimeMs IS NOT NULL")
    fun findAverageResponseTimeBySessionId(@Param("sessionId") sessionId: String): Double?
    
    /**
     * 세션 ID로 가장 많이 사용된 요청 타입 조회
     */
    @Query("""
        SELECT ch.requestType 
        FROM ConversationHistory ch 
        WHERE ch.sessionId = :sessionId 
        GROUP BY ch.requestType 
        ORDER BY COUNT(ch.requestType) DESC 
        LIMIT 1
    """, nativeQuery = true)
    fun findMostCommonRequestTypeBySessionId(@Param("sessionId") sessionId: String): String?
    
    /**
     * 세션 ID로 첫 번째 대화 시간 조회
     */
    @Query("SELECT MIN(ch.createdAt) FROM ConversationHistory ch WHERE ch.sessionId = :sessionId")
    fun findFirstConversationTimeBySessionId(@Param("sessionId") sessionId: String): LocalDateTime?
    
    /**
     * 세션 ID로 마지막 대화 시간 조회
     */
    @Query("SELECT MAX(ch.createdAt) FROM ConversationHistory ch WHERE ch.sessionId = :sessionId")
    fun findLastConversationTimeBySessionId(@Param("sessionId") sessionId: String): LocalDateTime?
    
    /**
     * 특정 기간보다 오래된 대화 기록 삭제
     */
    fun deleteByCreatedAtBefore(cutoffTime: LocalDateTime): Int

    /**
     * 세션 ID로 모든 대화 기록 삭제
     */
    fun deleteBySessionId(sessionId: String): Int
    
    /**
     * 모든 활성 세션 ID 조회 (최근 24시간 내 대화가 있는)
     */
    @Query("""
        SELECT DISTINCT ch.sessionId 
        FROM ConversationHistory ch 
        WHERE ch.createdAt >= :cutoffTime
    """)
    fun findActiveSessionIds(@Param("cutoffTime") cutoffTime: LocalDateTime): List<String>
}