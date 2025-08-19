package com.monkeys.client.client

import com.monkeys.client.dto.ChatRequest
import com.monkeys.client.dto.ChatResponse
import com.monkeys.client.dto.ServerData
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class McpServerClient(
    private val webClientBuilder: WebClient.Builder,
    @Value("\${mcp.server.url}") private val serverUrl: String
) {
    private val webClient = webClientBuilder.baseUrl(serverUrl).build()

    /**
     * MCP 서버로부터 데이터를 가져옵니다.
     * 서버는 /api/data 엔드포인트를 가지고 있다고 가정합니다.
     */
    fun fetchData(): Mono<ServerData> {
        return webClient.get()
            .uri("/api/data")
            .retrieve()
            .bodyToMono(ServerData::class.java)
    }

    /**
     * MCP 서버에 채팅 메시지를 전송하고 응답을 받습니다.
     */
    fun sendChatRequest(request: ChatRequest): Mono<ChatResponse> {
        return webClient.post()
            .uri("/api/chat")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ChatResponse::class.java)
    }
}
