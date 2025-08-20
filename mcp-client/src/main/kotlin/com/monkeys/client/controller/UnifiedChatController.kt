package com.monkeys.client.controller

import com.monkeys.client.service.UnifiedMcpService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://localhost:3003"]) // React 개발 서버용
class UnifiedChatController(
    private val chatClient: ChatClient,
    private val unifiedMcpService: UnifiedMcpService
) {
    private val logger = LoggerFactory.getLogger(UnifiedChatController::class.java)

    data class ChatRequest(val message: String)
    data class ChatResponse(val response: String, val timestamp: Long = System.currentTimeMillis())

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        logger.info("통합 채팅 요청: {}", request.message)
        
        return try {
            val response = chatClient.prompt()
                .user(buildPrompt(request.message))
                .tools(unifiedMcpService) // 모든 MCP 도구들 포함
                .call()
                .content() ?: "응답을 생성할 수 없습니다."
            
            logger.info("AI 응답 생성 완료")
            ResponseEntity.ok(ChatResponse(response))
        } catch (e: Exception) {
            logger.error("채팅 처리 중 오류: {}", e.message, e)
            ResponseEntity.status(500).body(
                ChatResponse("요청 처리 중 오류가 발생했습니다: ${e.message}")
            )
        }
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
                "github" to "http://localhost:8092",
                "jira" to "http://localhost:8093",
                "gmail" to "http://localhost:8094",
                "slack" to "http://localhost:8095"
            ),
            "timestamp" to System.currentTimeMillis()
        )
    }

    @PostMapping("/mcp-status")
    fun checkMcpStatus(): ResponseEntity<Map<String, Any>> {
        return try {
            val statusResult = unifiedMcpService.checkAllMcpServersStatus()
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "mcpServersStatus" to statusResult,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                mapOf(
                    "success" to false,
                    "error" to "MCP 서버 상태 확인 중 오류: ${e.message}",
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }

    private fun buildPrompt(userMessage: String): String {
        return """당신은 여러 외부 시스템에 접근할 수 있는 통합 AI 어시스턴트입니다.

사용 가능한 도구들:
- GitHub: 이슈 조회/생성, Pull Request 관리, 저장소 정보
- Jira: 프로젝트 이슈 관리, 스프린트 정보, 이슈 생성
- Gmail: 메일 조회/발송 (설정 필요)
- Slack: 메시지 전송/조회 (설정 필요)

사용자 요청: $userMessage

사용자의 요청을 분석하여 적절한 도구를 사용하고 도움이 되는 정보를 제공해주세요.
여러 시스템을 연계해야 하는 경우 순서대로 처리해주세요."""
    }
}