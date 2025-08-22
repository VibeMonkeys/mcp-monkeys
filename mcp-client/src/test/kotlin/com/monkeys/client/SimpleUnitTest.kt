package com.monkeys.client

import com.monkeys.client.service.SimpleUnifiedMcpService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("기본 단위 테스트 (Spring Context 없음)")
class SimpleUnitTest {

    @Test
    @DisplayName("SimpleUnifiedMcpService - GitHub 이슈 조회")
    fun `should return github issues`() {
        // Given
        val service = SimpleUnifiedMcpService(
            githubToken = "test-github-token",
            jiraUrl = "https://test.atlassian.net",
            jiraEmail = "test@example.com", 
            jiraToken = "test-jira-token",
            gmailClientId = "test-gmail-id",
            slackBotToken = "xoxb-test-token"
        )

        // When
        val issues = service.getGitHubIssues("facebook/react")

        // Then
        assertNotNull(issues)
        assertEquals(1, issues.size)
        assertEquals("테스트 이슈 (API 설정 필요)", issues[0].title)
        assertEquals("open", issues[0].state)
        assertEquals("system", issues[0].author)
    }

    @Test
    @DisplayName("SimpleUnifiedMcpService - GitHub 이슈 생성")
    fun `should create github issue`() {
        // Given
        val service = SimpleUnifiedMcpService(
            githubToken = "test-token",
            jiraUrl = "https://test.com",
            jiraEmail = "test@test.com",
            jiraToken = "test-token",
            gmailClientId = "test-id",
            slackBotToken = "xoxb-test-token"
        )

        // When
        val issue = service.createGitHubIssue("test/repo", "새로운 이슈", "이슈 설명")

        // Then
        assertNotNull(issue)
        assertEquals(99, issue.number)
        assertEquals("새로운 이슈", issue.title)
        assertEquals("이슈 설명", issue.body)
        assertEquals("open", issue.state)
        assertEquals("api-user", issue.author)
        assertTrue(issue.labels.contains("created-by-api"))
    }

    @Test
    @DisplayName("SimpleUnifiedMcpService - Jira 이슈 조회")
    fun `should return jira issues`() {
        // Given
        val service = SimpleUnifiedMcpService(
            githubToken = "test",
            jiraUrl = "https://test.com",
            jiraEmail = "test@test.com",
            jiraToken = "test",
            gmailClientId = "test",
            slackBotToken = "xoxb-test"
        )

        // When
        val issues = service.getJiraIssues("TEST")

        // Then
        assertNotNull(issues)
        assertEquals(1, issues.size)
        assertEquals("TEST-1", issues[0].key)
        assertEquals("테스트 이슈 (API 설정 필요)", issues[0].summary)
        assertEquals("Task", issues[0].issueType)
        assertEquals("To Do", issues[0].status)
    }

    @Test
    @DisplayName("SimpleUnifiedMcpService - Jira 이슈 생성")
    fun `should create jira issue`() {
        // Given
        val service = SimpleUnifiedMcpService(
            githubToken = "test",
            jiraUrl = "https://test.com",
            jiraEmail = "test@test.com",
            jiraToken = "test",
            gmailClientId = "test",
            slackBotToken = "xoxb-test"
        )

        // When
        val issue = service.createJiraIssue("PROJ", "새로운 작업", "작업 설명", "Story")

        // Then
        assertNotNull(issue)
        assertEquals("PROJ-99", issue.key)
        assertEquals("새로운 작업", issue.summary)
        assertEquals("작업 설명", issue.description)
        assertEquals("Story", issue.issueType)
        assertEquals("To Do", issue.status)
        assertEquals("api-user", issue.reporter)
        assertTrue(issue.labels.contains("created-by-api"))
    }

    @Test
    @DisplayName("SimpleUnifiedMcpService - API 상태 확인 (설정됨)")
    fun `should return configured status when tokens are set`() {
        // Given
        val service = SimpleUnifiedMcpService(
            githubToken = "real-github-token-12345",
            jiraUrl = "https://real-domain.atlassian.net",
            jiraEmail = "real@email.com", 
            jiraToken = "real-jira-token-12345",
            gmailClientId = "real-gmail-client-id",
            slackBotToken = "xoxb-real-slack-token"
        )
        
        // When
        val status = service.checkAllApiStatus()
        
        // Then
        assertNotNull(status)
        assertEquals("Ready (Token Set)", status.github)
        assertEquals("Ready (Credentials Set)", status.jira)
        assertEquals("Ready (OAuth Set)", status.gmail)
        assertEquals("Ready (Token Set)", status.slack)
        assertTrue(status.timestamp > 0)
    }

    @Test
    @DisplayName("SimpleUnifiedMcpService - API 상태 확인 (더미 데이터)")
    fun `should return not configured for dummy values`() {
        // Given
        val service = SimpleUnifiedMcpService(
            githubToken = "dummy-token",
            jiraUrl = "https://your-domain.atlassian.net",
            jiraEmail = "dummy@email.com",
            jiraToken = "dummy-token",
            gmailClientId = "dummy-client-id",
            slackBotToken = "xoxb-dummy-token"
        )

        // When
        val status = service.checkAllApiStatus()

        // Then
        assertEquals("Not Configured", status.github)
        assertEquals("Not Configured", status.jira)
        assertEquals("Not Configured", status.gmail)
        assertEquals("Not Configured", status.slack)
        assertTrue(status.timestamp > 0)
    }

    @Test
    @DisplayName("SimpleUnifiedMcpService - Gmail 메시지 조회")
    fun `should return gmail setup message`() {
        // Given
        val service = SimpleUnifiedMcpService(
            githubToken = "test",
            jiraUrl = "https://test.com",
            jiraEmail = "test@test.com",
            jiraToken = "test",
            gmailClientId = "test",
            slackBotToken = "xoxb-test"
        )

        // When
        val result = service.getGmailMessages(5, true)

        // Then
        assertNotNull(result)
        assertTrue(result.contains("Gmail API 설정이 필요합니다"))
        assertTrue(result.contains("GMAIL_CLIENT_ID"))
    }

    @Test
    @DisplayName("SimpleUnifiedMcpService - Slack 메시지 전송")
    fun `should return slack setup message`() {
        // Given
        val service = SimpleUnifiedMcpService(
            githubToken = "test",
            jiraUrl = "https://test.com",
            jiraEmail = "test@test.com",
            jiraToken = "test",
            gmailClientId = "test",
            slackBotToken = "xoxb-test"
        )

        // When
        val result = service.sendSlackMessage("#general", "테스트 메시지")

        // Then
        assertNotNull(result)
        assertTrue(result.contains("Slack API 설정이 필요합니다"))
        assertTrue(result.contains("SLACK_BOT_TOKEN"))
    }
}