package com.monkeys.client.service

import com.monkeys.client.client.McpServerClient
import com.monkeys.client.dto.ChatRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class McpService(
    private val mcpServerClient: McpServerClient
) {

    fun getAndProcessData(userInput: String): Mono<String> {
        return mcpServerClient.fetchData()
            .flatMap { serverData ->
                val chatRequest = ChatRequest(userInput)
                mcpServerClient.sendChatRequest(chatRequest)
            }
            .map { it.message }
    }
}
