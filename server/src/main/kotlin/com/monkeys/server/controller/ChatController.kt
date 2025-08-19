package com.monkeys.server.controller

import com.monkeys.server.client.GeminiClient
import com.monkeys.server.dto.ChatRequest
import com.monkeys.server.dto.ChatResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class ChatController(private val geminiClient: GeminiClient) {

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): Mono<ChatResponse> {
        return geminiClient.generateContent(request.prompt)
            .map { message -> ChatResponse(message) }
    }
}