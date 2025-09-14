# Function Calling 개선

## 📋 개요
Spring AI 1.0.1의 향상된 @Tool 어노테이션과 예외 처리를 활용하여 Function Calling 기능을 개선했습니다.

## 🔧 주요 개선사항

### 1. 커스텀 예외 클래스
```kotlin
class ToolExecutionException(
    message: String,
    val toolName: String,
    val errorCode: String = "TOOL_EXECUTION_ERROR",
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    
    fun getUserFriendlyMessage(): String {
        return when (errorCode) {
            "API_NOT_CONFIGURED" -> "API 설정이 필요합니다. 관리자에게 문의하세요."
            "NETWORK_ERROR" -> "네트워크 연결에 문제가 있습니다."
            "RATE_LIMIT_EXCEEDED" -> "API 요청 한도를 초과했습니다."
            else -> message
        }
    }
}
```

### 2. 특화된 예외 타입
- `GitHubApiException`: GitHub API 관련 에러
- `JiraApiException`: Jira API 관련 에러  
- `GmailApiException`: Gmail API 관련 에러
- `SlackApiException`: Slack API 관련 에러

### 3. 향상된 입력 검증
```kotlin
@Tool(description = "GitHub 저장소의 이슈 목록을 조회합니다")
fun getGitHubIssues(
    @ToolParam(description = "저장소 이름 (owner/repo 형식)", required = true)
    repository: String,
    // ...
): List<GitHubIssue> {
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
        
        // 실제 로직...
    } catch (e: GitHubApiException) {
        logger.error("GitHub 이슈 조회 실패: ${e.message}", e)
        throw e
    } catch (e: Exception) {
        logger.error("GitHub 이슈 조회 중 예상치 못한 오류: ${e.message}", e)
        throw GitHubApiException("GitHub 이슈 조회 중 오류가 발생했습니다", "UNKNOWN_ERROR", e)
    }
}
```

## ✨ 개선된 에러 처리

### Before (기존)
```kotlin
@Tool(description = "GitHub 저장소의 이슈 목록을 조회합니다")
fun getGitHubIssues(repository: String): List<GitHubIssue> {
    logger.info("GitHub 이슈 조회 요청: $repository")
    return listOf(/* mock data */)
}
```

### After (개선)
```kotlin
@Tool(description = "GitHub 저장소의 이슈 목록을 조회합니다")
fun getGitHubIssues(repository: String): List<GitHubIssue> {
    try {
        validateRepository(repository)
        checkApiConfiguration()
        return fetchGitHubIssues(repository)
    } catch (e: GitHubApiException) {
        throw e
    } catch (e: Exception) {
        throw GitHubApiException("Unexpected error", "UNKNOWN_ERROR", e)
    }
}
```

## 📊 에러 분류

### 1. 설정 관련 에러
- `API_NOT_CONFIGURED`: API 토큰 미설정
- `AUTHENTICATION_FAILED`: 인증 실패

### 2. 요청 관련 에러  
- `INVALID_PARAMETERS`: 잘못된 매개변수
- `RESOURCE_NOT_FOUND`: 리소스 없음

### 3. 외부 서비스 에러
- `NETWORK_ERROR`: 네트워크 연결 문제
- `RATE_LIMIT_EXCEEDED`: 요청 한도 초과

## 🎯 효과

### 개발자 경험
- **명확한 에러 메시지**: 구체적인 해결 방법 제시
- **타입별 예외 처리**: 각 API별 특화된 에러 핸들링
- **디버깅 개선**: 상세한 로그와 컨텍스트 정보

### 사용자 경험  
- **친화적 메시지**: 기술적 세부사항 숨김
- **해결 방안 제시**: "관리자에게 문의하세요" 등
- **일관된 에러 형식**: 모든 도구에서 동일한 에러 처리

## 🔗 관련 파일
- `mcp-client/src/main/kotlin/com/monkeys/client/exception/ToolExecutionException.kt`
- `mcp-client/src/main/kotlin/com/monkeys/client/service/SimpleUnifiedMcpService.kt`