package com.monkeys.client.client

import com.monkeys.client.dto.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@Component
class McpServerClient(
    private val webClientBuilder: WebClient.Builder,
    @Value("\${mcp.server.url:http://localhost:8080}") private val serverUrl: String
) {
    private val webClient = webClientBuilder.baseUrl(serverUrl).build()

    /**
     * MCP 서버에 채팅 메시지를 전송하고 응답을 받습니다.
     */
    fun sendChatMessage(message: String): Mono<McpChatResponse> {
        val request = McpChatRequest(userInput = message)
        return webClient.post()
            .uri("/api/chat")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(McpChatResponse::class.java)
            .onErrorReturn(McpChatResponse("서버 연결에 실패했습니다: 서버가 실행 중인지 확인해주세요"))
    }

    /**
     * MCP 서버의 채팅 기록을 조회합니다.
     */
    fun getChatHistory(): Mono<List<McpChatMessage>> {
        return webClient.get()
            .uri("/api/history")
            .retrieve()
            .bodyToFlux(McpChatMessage::class.java)
            .collectList()
            .onErrorReturn(emptyList())
    }

    /**
     * MCP 서버의 채팅 기록을 삭제합니다.
     */
    fun clearChatHistory(): Mono<Void> {
        return webClient.delete()
            .uri("/api/history")
            .retrieve()
            .bodyToMono(Void::class.java)
            .onErrorResume { Mono.empty() }
    }

    /**
     * MCP 서버의 사용 가능한 도구 목록을 조회합니다.
     */
    fun getAvailableTools(): Mono<Map<String, Any>> {
        return webClient.get()
            .uri("/api/tools")
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { it as Map<String, Any> }
            .onErrorReturn(emptyMap())
    }

    /**
     * MCP 서버의 상태를 확인합니다.
     */
    fun getServerStatus(): Mono<McpServerStatus> {
        return webClient.get()
            .uri("/actuator/health")
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { healthResponse ->
                val status = healthResponse["status"] as? String ?: "UNKNOWN"
                McpServerStatus(
                    status = if (status == "UP") "running" else "error",
                    version = "1.0.0"
                )
            }
            .onErrorReturn(McpServerStatus("disconnected", "unknown"))
    }

    /**
     * 서버 연결을 테스트합니다.
     */
    fun testConnection(): Mono<Boolean> {
        return webClient.get()
            .uri("/api/tools")
            .retrieve()
            .bodyToMono(String::class.java)
            .map { true }
            .onErrorReturn(false)
    }
}
