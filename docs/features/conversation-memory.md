# 대화 메모리/컨텍스트 관리

## 📋 개요
Spring AI 1.0.1의 ChatMemory 기능을 활용하여 대화 기록을 관리하고 컨텍스트를 유지합니다.

## 🏗️ 구조

### 1. ConversationHistory 엔티티
```kotlin
@Entity
data class ConversationHistory(
    @Id val id: String? = null,
    val sessionId: String,
    val userMessage: String,
    val aiResponse: String,
    val requestType: String = "general_chat",
    val responseTimeMs: Long? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

### 2. Repository 계층
```kotlin
interface ConversationHistoryRepository : JpaRepository<ConversationHistory, String> {
    fun findTop10BySessionIdOrderByCreatedAtDesc(sessionId: String): List<ConversationHistory>
    fun countBySessionId(sessionId: String): Long
    // ... 기타 쿼리 메서드들
}
```

### 3. 서비스 계층
```kotlin
@Service
class ConversationMemoryService {
    fun saveConversation(sessionId: String, userMessage: String, aiResponse: String)
    fun getRecentConversations(sessionId: String, limit: Int = 10): List<ConversationHistory>
    fun buildConversationContext(sessionId: String, maxMessages: Int = 5): String
    fun getConversationStats(sessionId: String): ConversationStats?
}
```

## ✨ 주요 기능

### 1. 자동 대화 저장
- 모든 대화 내용 비동기 저장
- 응답 시간, 요청 타입 등 메타데이터 포함

### 2. 컨텍스트 구성
```kotlin
fun buildConversationContext(sessionId: String, maxMessages: Int = 5): String {
    val recentConversations = getRecentConversations(sessionId, maxMessages)
    return recentConversations.reversed().joinToString("\n\n") { 
        "사용자: ${it.userMessage}\nAI: ${it.aiResponse}" 
    }
}
```

### 3. 대화 통계
- 총 대화 수, 평균 응답 시간
- 가장 많이 사용된 요청 타입
- 첫 번째/마지막 대화 시간

### 4. 자동 정리
```kotlin
@Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
fun cleanupOldConversations() {
    val cutoffTime = LocalDateTime.now().minusDays(30)
    conversationHistoryRepository.deleteByCreatedAtBefore(cutoffTime)
}
```

## 📊 효과

### 사용자 경험
- **대화 연속성**: 이전 대화 내용 기억
- **개인화**: 사용자별 대화 패턴 분석
- **컨텍스트 인식**: 문맥에 맞는 응답

### 분석 능력
- **사용 패턴**: 세션별 대화 빈도 분석
- **성능 추적**: 응답 시간 트렌드
- **인기 기능**: 가장 많이 사용되는 요청 타입

## 🔗 관련 파일
- `mcp-client/src/main/kotlin/com/monkeys/client/entity/ConversationHistory.kt`
- `mcp-client/src/main/kotlin/com/monkeys/client/repository/ConversationHistoryRepository.kt`
- `mcp-client/src/main/kotlin/com/monkeys/client/service/ConversationMemoryService.kt`