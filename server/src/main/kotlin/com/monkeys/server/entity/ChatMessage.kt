package com.monkeys.server.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class ChatMessage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val role: String,

    @Column(nullable = false, length = 2048)
    val content: String,

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now()
)