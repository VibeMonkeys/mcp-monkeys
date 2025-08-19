package com.monkeys.client.service

import com.monkeys.client.client.McpServerClient
import com.monkeys.client.dto.ChatRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class McpService(
    private val mcpServerClient: McpServerClient
) {

    fun getAndProcessData(userInput: String, history: List<String>): Mono<String> {
        return mcpServerClient.fetchData()
            .flatMap { serverData ->
                // 대화 기록을 문자열로 변환합니다.
                val historyText = history.joinToString("\n")

                // 서버 컨텍스트, 대화 기록, 새 질문을 모두 포함하는 프롬프트를 생성합니다.
                val prompt = """You are a helpful assistant.

### Base Context
${serverData.content}

### Conversation History
$historyText

### New Question
$userInput

Based on the context and history, please provide a helpful and friendly answer to the new question.
Answer:"""

                val chatRequest = ChatRequest(prompt)
                mcpServerClient.sendChatRequest(chatRequest)
            }
            .map { it.message }
    }
}
