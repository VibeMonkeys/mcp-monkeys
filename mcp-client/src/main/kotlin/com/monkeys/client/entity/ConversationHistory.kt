package com.monkeys.client.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 대화 기록을 저장하는 엔티티
 */
@Entity
@Table(name = "conversation_history")
data class ConversationHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @Column(name = "session_id", nullable = false, length = 255)
    val sessionId: String,
    
    @Column(name = "user_message", columnDefinition = "TEXT")
    val userMessage: String,
    
    @Column(name = "ai_response", columnDefinition = "TEXT")
    val aiResponse: String,
    
    @Column(name = "request_type", length = 50)
    val requestType: String = "general_chat",
    
    @Column(name = "response_time_ms")
    val responseTimeMs: Long? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null
) {
    // JPA를 위한 기본 생성자
    constructor() : this(
        sessionId = "",
        userMessage = "",
        aiResponse = ""
    )
}

/**
 * 대화 통계 정보
 */
data class ConversationStats(
    val sessionId: String,
    val totalConversations: Long,
    val averageResponseTime: Double,
    val firstConversationAt: LocalDateTime,
    val lastConversationAt: LocalDateTime,
    val mostCommonRequestType: String
)