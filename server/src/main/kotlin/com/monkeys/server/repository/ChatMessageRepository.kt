package com.monkeys.server.repository

import com.monkeys.server.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findTop20ByOrderByIdAsc(): List<ChatMessage>
}