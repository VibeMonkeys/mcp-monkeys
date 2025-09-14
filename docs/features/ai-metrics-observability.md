# AI 전용 관찰성 강화

## 📋 개요
Spring AI 1.0.1과 Micrometer를 활용하여 AI 모델별 세분화된 메트릭을 수집합니다.

## 📊 수집 메트릭

### 1. 기본 요청 메트릭
```kotlin
private val aiRequestCounter = Counter.builder("ai.requests.total")
    .description("Total number of AI requests")
    .register(meterRegistry)

private val aiResponseTimer = Timer.builder("ai.response.duration")
    .description("AI response time distribution")
    .register(meterRegistry)
```

### 2. 모델별 성능 메트릭
- **모델명**: `gemini-1.5-flash`
- **요청 타입**: `general_chat`, `structured`, `streaming`
- **세션 타입**: `user`, `test`, `demo`, `default`

### 3. 토큰 사용량 추적
```kotlin
private val tokenUsageCounter = Counter.builder("ai.tokens.consumed")
    .description("Total tokens consumed by AI models")
    .register(meterRegistry)
```

### 4. 에러 분류 메트릭
- `timeout`: 타임아웃 에러
- `rate_limit`: 요청 한도 초과
- `auth`: 인증 에러
- `quota`: 할당량 초과
- `network`: 네트워크 에러

## 🔧 사용 방법

### 1. 요청 시작
```kotlin
val metricsSample = aiMetricsService.recordRequest(
    modelName = "gemini-1.5-flash",
    requestType = "general_chat", 
    sessionId = sessionId
)
```

### 2. 응답 완료
```kotlin
aiMetricsService.recordResponse(
    sample = metricsSample,
    modelName = "gemini-1.5-flash",
    requestType = "general_chat",
    responseLength = response.length,
    success = true,
    tokenCount = estimatedTokens
)
```

### 3. 에러 기록
```kotlin
aiMetricsService.recordError(
    modelName = "gemini-1.5-flash",
    requestType = "general_chat",
    errorType = "ChatServiceException",
    errorMessage = e.message
)
```

## 📈 대시보드 예시

### Prometheus 쿼리
```promql
# 모델별 요청 수
sum(rate(ai_requests_total[5m])) by (model)

# 평균 응답 시간
rate(ai_response_duration_sum[5m]) / rate(ai_response_duration_count[5m])

# 에러율
sum(rate(ai_errors_total[5m])) / sum(rate(ai_requests_total[5m])) * 100
```

## 🔗 관련 파일
- `mcp-client/src/main/kotlin/com/monkeys/client/service/AiMetricsService.kt`