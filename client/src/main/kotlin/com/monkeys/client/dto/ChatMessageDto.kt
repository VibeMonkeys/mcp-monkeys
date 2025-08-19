package com.monkeys.client.dto

import java.time.LocalDateTime

data class ChatMessageDto(
    val id: Long,
    val role: String,
    val content: String,
    val timestamp: LocalDateTime
)