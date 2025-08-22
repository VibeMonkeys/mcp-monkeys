package com.monkeys.client.controller

import com.monkeys.client.dto.*
import com.monkeys.client.service.SimpleUnifiedMcpService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import org.springframework.http.MediaType

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://localhost:3004", "http://localhost:3000"]) // React 개발 서버용
class UnifiedChatController(
    private val chatClient: ChatClient,
    private val unifiedMcpService: SimpleUnifiedMcpService
) {
    private val logger = LoggerFactory.getLogger(UnifiedChatController::class.java)

    data class ChatRequest(
        val message: String, 
        val sessionId: String? = null,
        val format: String? = null // "structured" 또는 "text"
    )
    data class ChatResponse(val response: String, val timestamp: Long = System.currentTimeMillis())
    data class StructuredChatResponse(val data: Any, val format: String, val timestamp: Long = System.currentTimeMillis())

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): ResponseEntity<Any> {
        logger.info("통합 채팅 요청: {}", request.message)
        
        return try {
            // 세션 ID 설정
            val sessionId = request.sessionId ?: "default-session"
            
            val chatBuilder = chatClient.prompt()
                .user(buildPrompt(request.message))
                .tools(unifiedMcpService)
            
            // Structured Output 요청 처리
            if (request.format == "structured") {
                val responseType = determineResponseType(request.message)
                val converter = BeanOutputConverter(responseType)
                
                val structuredResponse = chatBuilder
                    .user("${request.message}\n\n${converter.format}")
                    .call()
                    .entity(responseType)
                
                ResponseEntity.ok(StructuredChatResponse(structuredResponse ?: "No response", "structured"))
            } else {
                val response = chatBuilder
                    .call()
                    .content() ?: "응답을 생성할 수 없습니다."
                
                ResponseEntity.ok(ChatResponse(response))
            }
        } catch (e: Exception) {
            logger.error("채팅 처리 중 오류: {}", e.message, e)
            ResponseEntity.status(500).body(
                ChatResponse("요청 처리 중 오류가 발생했습니다: ${e.message}")
            )
        }
    }
    
    @GetMapping(value = ["/chat/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun chatStream(@RequestParam message: String, @RequestParam(required = false) sessionId: String?): Flux<String> {
        logger.info("스트리밍 채팅 요청: {}", message)
        
        val actualSessionId = sessionId ?: "default-session"
        
        return chatClient.prompt()
            .user(buildPrompt(message))
            .tools(unifiedMcpService)
            .stream()
            .content()
            .map { chunk -> "data: $chunk\n\n" }
            .onErrorReturn("data: [ERROR] 스트리밍 중 오류가 발생했습니다.\n\n")
    }

    @GetMapping("/tools")
    fun getAvailableTools(): Map<String, Map<String, String>> {
        return mapOf(
            "GitHub" to mapOf(
                "getGitHubIssues" to "GitHub 저장소의 이슈 목록을 조회합니다",
                "createGitHubIssue" to "GitHub 저장소에 새로운 이슈를 생성합니다"
            ),
            "Jira" to mapOf(
                "getJiraIssues" to "Jira 프로젝트의 이슈 목록을 조회합니다",
                "createJiraIssue" to "Jira에 새로운 이슈를 생성합니다"
            ),
            "Gmail" to mapOf(
                "getGmailMessages" to "Gmail 받은편지함의 메일 목록을 조회합니다",
                "sendGmailMessage" to "Gmail로 메일을 발송합니다"
            ),
            "Slack" to mapOf(
                "sendSlackMessage" to "Slack 채널에 메시지를 전송합니다",
                "getSlackMessages" to "Slack 채널의 최근 메시지를 조회합니다"
            ),
            "System" to mapOf(
                "checkAllMcpServersStatus" to "모든 MCP 서버의 상태를 확인합니다"
            )
        )
    }

    @GetMapping("/status")
    fun getStatus(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "service" to "Unified MCP Client",
            "version" to "1.0.0",
            "mcpServers" to mapOf(
                "weather" to "http://localhost:8092",
                "news" to "http://localhost:8093", 
                "translate" to "http://localhost:8094",
                "calendar" to "http://localhost:8095"
            ),
            "timestamp" to System.currentTimeMillis()
        )
    }

    @PostMapping("/api-status")
    fun checkApiStatus(): ResponseEntity<Map<String, Any>> {
        return try {
            val statusResult = unifiedMcpService.checkAllApiStatus()
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "apiStatus" to statusResult,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                mapOf(
                    "success" to false,
                    "error" to "API 상태 확인 중 오류: ${e.message}",
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }

    private fun determineResponseType(message: String): Class<*> {
        return when {
            message.contains("날씨", ignoreCase = true) || 
            message.contains("weather", ignoreCase = true) -> WeatherResponse::class.java
            
            message.contains("뉴스", ignoreCase = true) || 
            message.contains("news", ignoreCase = true) -> NewsResponse::class.java
            
            message.contains("번역", ignoreCase = true) || 
            message.contains("translate", ignoreCase = true) -> TranslationResponse::class.java
            
            message.contains("일정", ignoreCase = true) || 
            message.contains("calendar", ignoreCase = true) -> CalendarResponse::class.java
            
            else -> MultiServiceResponse::class.java
        }
    }
    
    private fun buildPrompt(userMessage: String): String {
        return """
사용자 요청: $userMessage

요청을 분석하여 적절한 도구를 사용하고 도움이 되는 정보를 제공해주세요.
여러 시스템을 연계해야 하는 경우 순서대로 처리해주세요.
        """.trimIndent()
    }
}