package com.monkeys.client.service

import com.monkeys.shared.dto.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import okhttp3.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@Service
class UnifiedMcpService(
    @Value("\${mcp.github.url:http://localhost:8092}") private val githubUrl: String,
    @Value("\${mcp.jira.url:http://localhost:8093}") private val jiraUrl: String,
    @Value("\${mcp.gmail.url:http://localhost:8094}") private val gmailUrl: String,
    @Value("\${mcp.slack.url:http://localhost:8095}") private val slackUrl: String
) {
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()

    // GitHub Tools
    @Tool(description = "GitHub 저장소의 이슈 목록을 조회합니다")
    fun getGitHubIssues(
        @ToolParam(description = "저장소 이름 (owner/repo 형식)", required = true)
        repository: String,
        @ToolParam(description = "이슈 상태 (open, closed, all)")
        state: String = "open",
        @ToolParam(description = "조회할 이슈 개수")
        limit: Int = 10
    ): String {
        return callMcpServer(githubUrl, "github", "getIssues", mapOf(
            "repository" to repository,
            "state" to state,
            "limit" to limit
        ))
    }

    @Tool(description = "GitHub 저장소에 새로운 이슈를 생성합니다")
    fun createGitHubIssue(
        @ToolParam(description = "저장소 이름 (owner/repo 형식)", required = true)
        repository: String,
        @ToolParam(description = "이슈 제목", required = true)
        title: String,
        @ToolParam(description = "이슈 내용")
        body: String = ""
    ): String {
        return callMcpServer(githubUrl, "github", "createIssue", mapOf(
            "repository" to repository,
            "title" to title,
            "body" to body
        ))
    }

    // Jira Tools
    @Tool(description = "Jira 프로젝트의 이슈 목록을 조회합니다")
    fun getJiraIssues(
        @ToolParam(description = "프로젝트 키 (예: PROJ)", required = true)
        projectKey: String,
        @ToolParam(description = "이슈 타입 (Bug, Task, Story 등)")
        issueType: String = "",
        @ToolParam(description = "조회할 이슈 개수")
        maxResults: Int = 10
    ): String {
        return callMcpServer(jiraUrl, "jira", "getIssues", mapOf(
            "projectKey" to projectKey,
            "issueType" to issueType,
            "maxResults" to maxResults
        ))
    }

    @Tool(description = "Jira에 새로운 이슈를 생성합니다")
    fun createJiraIssue(
        @ToolParam(description = "프로젝트 키 (예: PROJ)", required = true)
        projectKey: String,
        @ToolParam(description = "이슈 제목", required = true)
        summary: String,
        @ToolParam(description = "이슈 설명")
        description: String = "",
        @ToolParam(description = "이슈 타입")
        issueType: String = "Task"
    ): String {
        return callMcpServer(jiraUrl, "jira", "createIssue", mapOf(
            "projectKey" to projectKey,
            "summary" to summary,
            "description" to description,
            "issueType" to issueType
        ))
    }

    // Gmail Tools (Mock implementation)
    @Tool(description = "Gmail 받은편지함의 메일 목록을 조회합니다")
    fun getGmailMessages(
        @ToolParam(description = "조회할 메일 개수")
        maxResults: Int = 10,
        @ToolParam(description = "읽지 않은 메일만 조회")
        unreadOnly: Boolean = false
    ): String {
        return "Gmail API 연동이 필요합니다. 테스트용 더미 데이터를 반환합니다."
    }

    @Tool(description = "Gmail로 메일을 발송합니다")
    fun sendGmailMessage(
        @ToolParam(description = "수신자 이메일", required = true)
        to: String,
        @ToolParam(description = "메일 제목", required = true)
        subject: String,
        @ToolParam(description = "메일 내용", required = true)
        body: String
    ): String {
        return "Gmail API 연동이 필요합니다. 실제 메일 발송 기능은 구현되지 않았습니다."
    }

    // Slack Tools (Mock implementation)
    @Tool(description = "Slack 채널에 메시지를 전송합니다")
    fun sendSlackMessage(
        @ToolParam(description = "채널 이름 또는 ID", required = true)
        channel: String,
        @ToolParam(description = "전송할 메시지", required = true)
        text: String
    ): String {
        return "Slack API 연동이 필요합니다. 실제 메시지 전송 기능은 구현되지 않았습니다."
    }

    @Tool(description = "Slack 채널의 최근 메시지를 조회합니다")
    fun getSlackMessages(
        @ToolParam(description = "채널 이름 또는 ID", required = true)
        channel: String,
        @ToolParam(description = "조회할 메시지 개수")
        limit: Int = 10
    ): String {
        return "Slack API 연동이 필요합니다. 테스트용 더미 데이터를 반환합니다."
    }

    // 유틸리티 Tools
    @Tool(description = "사용 가능한 모든 MCP 서버의 상태를 확인합니다")
    fun checkAllMcpServersStatus(): String {
        val statuses = mutableMapOf<String, String>()
        
        try {
            statuses["GitHub"] = checkServerHealth(githubUrl)
            statuses["Jira"] = checkServerHealth(jiraUrl)
            statuses["Gmail"] = checkServerHealth(gmailUrl)
            statuses["Slack"] = checkServerHealth(slackUrl)
        } catch (e: Exception) {
            statuses["Error"] = "서버 상태 확인 중 오류: ${e.message}"
        }
        
        return mapper.writeValueAsString(statuses)
    }

    // MCP 서버 호출 공통 함수
    private fun callMcpServer(serverUrl: String, service: String, method: String, params: Map<String, Any>): String {
        return try {
            // 실제로는 MCP 서버의 적절한 엔드포인트를 호출해야 함
            // 여기서는 더미 응답 반환
            "$service MCP 서버의 $method 메소드 호출됨. 매개변수: ${mapper.writeValueAsString(params)}"
        } catch (e: Exception) {
            "$service MCP 서버 호출 중 오류: ${e.message}"
        }
    }

    private fun checkServerHealth(serverUrl: String): String {
        return try {
            val request = Request.Builder()
                .url("$serverUrl/actuator/health")
                .build()
                
            val response = client.newCall(request).execute()
            if (response.isSuccessful) "UP" else "DOWN"
        } catch (e: Exception) {
            "UNKNOWN"
        }
    }
}