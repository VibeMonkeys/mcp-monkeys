package com.monkeys.client.service

import com.monkeys.shared.dto.*
import com.monkeys.client.exception.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class SimpleUnifiedMcpService(
    @Value("\${api.github.token:dummy-token}") private val githubToken: String,
    @Value("\${api.jira.url:https://your-domain.atlassian.net}") private val jiraUrl: String,
    @Value("\${api.jira.email:dummy@email.com}") private val jiraEmail: String,
    @Value("\${api.jira.token:dummy-token}") private val jiraToken: String,
    @Value("\${api.gmail.client-id:dummy-client-id}") private val gmailClientId: String,
    @Value("\${api.slack.bot-token:xoxb-dummy-token}") private val slackBotToken: String
) {
    private val logger = LoggerFactory.getLogger(SimpleUnifiedMcpService::class.java)

    // GitHub Tools (Simplified)
    @Tool(description = "GitHub 저장소의 이슈 목록을 조회합니다")
    fun getGitHubIssues(
        @ToolParam(description = "저장소 이름 (owner/repo 형식)", required = true)
        repository: String,
        @ToolParam(description = "이슈 상태 (open, closed, all)")
        state: String = "open",
        @ToolParam(description = "조회할 이슈 개수")
        limit: Int = 10
    ): List<GitHubIssue> {
        logger.info("GitHub 이슈 조회 요청: $repository")
        
        try {
            // 입력 검증
            if (!repository.contains("/") || repository.split("/").size != 2) {
                throw GitHubApiException(
                    "저장소 형식이 올바르지 않습니다. 'owner/repo' 형식으로 입력해주세요.",
                    "INVALID_PARAMETERS"
                )
            }
            
            // API 설정 확인
            if (githubToken == "dummy-token") {
                throw GitHubApiException(
                    "GitHub API 토큰이 설정되지 않았습니다.",
                    "API_NOT_CONFIGURED"
                )
            }
            
            // Mock 데이터 반환 (실제 API 호출로 대체 가능)
            return listOf(
                GitHubIssue(
                    number = 1,
                    title = "테스트 이슈 (API 설정 필요)",
                    body = "GitHub API 토큰을 설정하면 실제 데이터를 가져올 수 있습니다.",
                    state = state,
                    author = "system",
                    createdAt = "2024-01-01T00:00:00Z",
                    labels = listOf("test", "configuration-needed")
                )
            )
        } catch (e: GitHubApiException) {
            logger.error("GitHub 이슈 조회 실패: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            logger.error("GitHub 이슈 조회 중 예상치 못한 오류: ${e.message}", e)
            throw GitHubApiException("GitHub 이슈 조회 중 오류가 발생했습니다", "UNKNOWN_ERROR", e)
        }
    }

    @Tool(description = "GitHub 저장소에 새로운 이슈를 생성합니다")
    fun createGitHubIssue(
        @ToolParam(description = "저장소 이름 (owner/repo 형식)", required = true)
        repository: String,
        @ToolParam(description = "이슈 제목", required = true)
        title: String,
        @ToolParam(description = "이슈 내용")
        body: String = ""
    ): GitHubIssue {
        logger.info("GitHub 이슈 생성 요청: $title")
        
        try {
            // 입력 검증
            if (!repository.contains("/") || repository.split("/").size != 2) {
                throw GitHubApiException(
                    "저장소 형식이 올바르지 않습니다. 'owner/repo' 형식으로 입력해주세요.",
                    "INVALID_PARAMETERS"
                )
            }
            
            if (title.isBlank()) {
                throw GitHubApiException(
                    "이슈 제목은 필수입니다.",
                    "INVALID_PARAMETERS"
                )
            }
            
            // API 설정 확인
            if (githubToken == "dummy-token") {
                throw GitHubApiException(
                    "GitHub API 토큰이 설정되지 않았습니다.",
                    "API_NOT_CONFIGURED"
                )
            }
            
            return GitHubIssue(
                number = 99,
                title = title,
                body = body,
                state = "open",
                author = "api-user",
                createdAt = "2024-01-01T00:00:00Z",
                labels = listOf("created-by-api")
            )
        } catch (e: GitHubApiException) {
            logger.error("GitHub 이슈 생성 실패: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            logger.error("GitHub 이슈 생성 중 예상치 못한 오류: ${e.message}", e)
            throw GitHubApiException("GitHub 이슈 생성 중 오류가 발생했습니다", "UNKNOWN_ERROR", e)
        }
    }

    // Jira Tools (Simplified)
    @Tool(description = "Jira 프로젝트의 이슈 목록을 조회합니다")
    fun getJiraIssues(
        @ToolParam(description = "프로젝트 키 (예: PROJ)", required = true)
        projectKey: String,
        @ToolParam(description = "이슈 타입 (Bug, Task, Story 등)")
        issueType: String = "",
        @ToolParam(description = "조회할 이슈 개수")
        maxResults: Int = 10
    ): List<JiraIssue> {
        logger.info("Jira 이슈 조회 요청: $projectKey")
        return listOf(
            JiraIssue(
                key = "$projectKey-1",
                summary = "테스트 이슈 (API 설정 필요)",
                description = "Jira API 설정을 완료하면 실제 데이터를 가져올 수 있습니다.",
                issueType = if (issueType.isNotEmpty()) issueType else "Task",
                status = "To Do",
                priority = "Medium",
                assignee = null,
                reporter = "system",
                createdAt = "2024-01-01T00:00:00.000+0000",
                updatedAt = "2024-01-01T00:00:00.000+0000",
                labels = listOf("test", "configuration-needed")
            )
        )
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
    ): JiraIssue {
        logger.info("Jira 이슈 생성 요청: $summary")
        return JiraIssue(
            key = "$projectKey-99",
            summary = summary,
            description = description,
            issueType = issueType,
            status = "To Do",
            priority = "Medium",
            assignee = null,
            reporter = "api-user",
            createdAt = "2024-01-01T00:00:00.000+0000",
            updatedAt = "2024-01-01T00:00:00.000+0000",
            labels = listOf("created-by-api")
        )
    }

    // Gmail Tools (Simplified)
    @Tool(description = "Gmail 받은편지함의 메일 목록을 조회합니다")
    fun getGmailMessages(
        @ToolParam(description = "조회할 메일 개수")
        maxResults: Int = 10,
        @ToolParam(description = "읽지 않은 메일만 조회")
        unreadOnly: Boolean = false
    ): String {
        logger.info("Gmail 메시지 조회 요청")
        return "Gmail API 설정이 필요합니다. GMAIL_CLIENT_ID, GMAIL_CLIENT_SECRET, GMAIL_REFRESH_TOKEN 환경변수를 설정해주세요."
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
        logger.info("Gmail 메시지 발송 요청: $subject")
        return "Gmail API 설정이 필요합니다. 설정 완료 후 실제 메일을 발송할 수 있습니다."
    }

    // Slack Tools (Simplified)
    @Tool(description = "Slack 채널에 메시지를 전송합니다")
    fun sendSlackMessage(
        @ToolParam(description = "채널 이름 또는 ID", required = true)
        channel: String,
        @ToolParam(description = "전송할 메시지", required = true)
        text: String
    ): String {
        logger.info("Slack 메시지 전송 요청: $channel")
        return "Slack API 설정이 필요합니다. SLACK_BOT_TOKEN 환경변수를 설정해주세요."
    }

    @Tool(description = "Slack 채널의 최근 메시지를 조회합니다")
    fun getSlackMessages(
        @ToolParam(description = "채널 이름 또는 ID", required = true)
        channel: String,
        @ToolParam(description = "조회할 메시지 개수")
        limit: Int = 10
    ): String {
        logger.info("Slack 메시지 조회 요청: $channel")
        return "Slack API 설정이 필요합니다. 설정 완료 후 실제 메시지를 조회할 수 있습니다."
    }

    // 유틸리티 Tools
    @Tool(description = "모든 API 연동 상태를 확인합니다")
    fun checkAllApiStatus(): ApiStatusResult {
        logger.info("API 상태 확인 요청")
        
        return ApiStatusResult(
            github = if (githubToken != "dummy-token") "Ready (Token Set)" else "Not Configured",
            jira = if (jiraToken != "dummy-token" && !jiraUrl.contains("your-domain")) "Ready (Credentials Set)" else "Not Configured",
            gmail = if (gmailClientId != "dummy-client-id") "Ready (OAuth Set)" else "Not Configured",
            slack = if (slackBotToken != "xoxb-dummy-token") "Ready (Token Set)" else "Not Configured",
            timestamp = System.currentTimeMillis()
        )
    }
}