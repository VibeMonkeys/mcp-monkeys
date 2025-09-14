package com.monkeys.client.exception

/**
 * Tool 실행 중 발생하는 예외를 나타내는 커스텀 예외 클래스
 */
open class ToolExecutionException(
    message: String,
    val toolName: String,
    val errorCode: String = "TOOL_EXECUTION_ERROR",
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    
    /**
     * 사용자 친화적인 에러 메시지 생성
     */
    fun getUserFriendlyMessage(): String {
        return when (errorCode) {
            "API_NOT_CONFIGURED" -> "API 설정이 필요합니다. 관리자에게 문의하세요."
            "NETWORK_ERROR" -> "네트워크 연결에 문제가 있습니다. 잠시 후 다시 시도해주세요."
            "RATE_LIMIT_EXCEEDED" -> "API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."
            "INVALID_PARAMETERS" -> "입력된 매개변수가 올바르지 않습니다."
            "AUTHENTICATION_FAILED" -> "인증에 실패했습니다. API 설정을 확인해주세요."
            "RESOURCE_NOT_FOUND" -> "요청한 리소스를 찾을 수 없습니다."
            else -> message ?: "알 수 없는 오류가 발생했습니다"
        }
    }
}

/**
 * GitHub API 관련 예외
 */
class GitHubApiException(
    message: String,
    errorCode: String = "GITHUB_API_ERROR",
    cause: Throwable? = null
) : ToolExecutionException(message, "GitHub", errorCode, cause)

/**
 * Jira API 관련 예외
 */
class JiraApiException(
    message: String,
    errorCode: String = "JIRA_API_ERROR",
    cause: Throwable? = null
) : ToolExecutionException(message, "Jira", errorCode, cause)

/**
 * Gmail API 관련 예외
 */
class GmailApiException(
    message: String,
    errorCode: String = "GMAIL_API_ERROR",
    cause: Throwable? = null
) : ToolExecutionException(message, "Gmail", errorCode, cause)

/**
 * Slack API 관련 예외
 */
class SlackApiException(
    message: String,
    errorCode: String = "SLACK_API_ERROR",
    cause: Throwable? = null
) : ToolExecutionException(message, "Slack", errorCode, cause)