package com.monkeys.server.controller

import com.monkeys.server.client.GeminiClient
import com.monkeys.server.dto.ChatRequest
import com.monkeys.server.dto.ChatResponse
import com.monkeys.server.entity.ChatMessage
import com.monkeys.server.repository.ChatMessageRepository
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@RestController
@RequestMapping("/api")
class ChatController(
    private val geminiClient: GeminiClient,
    private val chatMessageRepository: ChatMessageRepository
) {

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): Mono<ChatResponse> {
        // 1. 사용자 메시지 저장
        val userMessage = ChatMessage(role = "USER", content = request.userInput, timestamp = LocalDateTime.now())
        chatMessageRepository.save(userMessage)

        // 2. 이전 대화 기록 불러오기 (최근 20개 메시지)
        val history = chatMessageRepository.findTop20ByOrderByIdAsc()

        // 3. 프롬프트 구성
        val historyText = history.joinToString("\n") { "${it.role}: ${it.content}" }
        val prompt = """You are a helpful assistant.

### Conversation History
$historyText

### New Question
${request.userInput}

Based on the history, please provide a helpful and friendly answer to the new question.
Answer:"""

        // 4. Gemini에 요청
        return geminiClient.generateContent(prompt)
            .map {
                // 5. Gemini 응답 저장
                val geminiResponse = ChatMessage(role = "MODEL", content = it, timestamp = LocalDateTime.now())
                chatMessageRepository.save(geminiResponse)
                ChatResponse(it)
            }
    }

    @GetMapping("/history")
    fun getChatHistory(): List<ChatMessage> {
        return chatMessageRepository.findAll()
    }

    @DeleteMapping("/history")
    fun clearChatHistory() {
        chatMessageRepository.deleteAll()
    }
}
