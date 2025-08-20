package com.monkeys.client.controller

import com.monkeys.client.dto.*
import com.monkeys.client.service.McpService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/client")
class McpClientController(private val mcpService: McpService) {

    @PostMapping("/chat")
    fun chat(@RequestBody request: McpChatRequest): Mono<ResponseEntity<Map<String, Any>>> {
        return mcpService.getAndProcessData(request.userInput)
            .map { response ->
                ResponseEntity.ok(
                    mapOf<String, Any>(
                        "success" to true,
                        "response" to response,
                        "timestamp" to System.currentTimeMillis(),
                        "originalRequest" to request.userInput
                    )
                )
            }
            .onErrorReturn(
                ResponseEntity.status(500).body(
                    mapOf<String, Any>(
                        "success" to false,
                        "error" to "서버 통신 중 오류가 발생했습니다",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            )
    }

    @GetMapping("/history")
    fun getHistory(): Mono<ResponseEntity<Map<String, Any>>> {
        return mcpService.getChatHistory()
            .map { history ->
                ResponseEntity.ok(
                    mapOf<String, Any>(
                        "success" to true,
                        "history" to history,
                        "count" to history.size,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
            .onErrorReturn(
                ResponseEntity.status(500).body(
                    mapOf<String, Any>(
                        "success" to false,
                        "error" to "채팅 기록 조회 중 오류가 발생했습니다",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            )
    }

    @DeleteMapping("/history")
    fun clearHistory(): Mono<ResponseEntity<Map<String, Any>>> {
        return mcpService.clearChatHistory()
            .then(
                Mono.just(
                    ResponseEntity.ok(
                        mapOf<String, Any>(
                            "success" to true,
                            "message" to "채팅 기록이 성공적으로 삭제되었습니다",
                            "timestamp" to System.currentTimeMillis()
                        )
                    )
                )
            )
            .onErrorReturn(
                ResponseEntity.status(500).body(
                    mapOf<String, Any>(
                        "success" to false,
                        "error" to "채팅 기록 삭제 중 오류가 발생했습니다",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            )
    }

    @GetMapping("/tools")
    fun getAvailableTools(): Mono<ResponseEntity<Map<String, Any>>> {
        return mcpService.getAvailableTools()
            .map { tools ->
                ResponseEntity.ok(
                    mapOf<String, Any>(
                        "success" to true,
                        "tools" to tools,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
            .onErrorReturn(
                ResponseEntity.status(500).body(
                    mapOf<String, Any>(
                        "success" to false,
                        "error" to "도구 목록 조회 중 오류가 발생했습니다",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            )
    }

    @GetMapping("/server-status")
    fun getServerStatus(): Mono<ResponseEntity<Map<String, Any>>> {
        return mcpService.getServerStatus()
            .map { status ->
                ResponseEntity.ok(
                    mapOf<String, Any>(
                        "success" to true,
                        "serverStatus" to status,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
            .onErrorReturn(
                ResponseEntity.status(500).body(
                    mapOf<String, Any>(
                        "success" to false,
                        "error" to "서버 상태 조회 중 오류가 발생했습니다",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            )
    }

    @GetMapping("/status")
    fun getClientStatus(): Mono<ResponseEntity<Map<String, Any>>> {
        return mcpService.testServerConnection()
            .map { connected ->
                ResponseEntity.ok(
                    mapOf<String, Any>(
                        "success" to true,
                        "clientStatus" to "running",
                        "serverConnected" to connected,
                        "service" to "MCP Client API",
                        "version" to "1.0.0",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
            .onErrorReturn(
                ResponseEntity.ok(
                    mapOf<String, Any>(
                        "success" to true,
                        "clientStatus" to "running",
                        "serverConnected" to false,
                        "service" to "MCP Client API",
                        "version" to "1.0.0",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            )
    }
}