---
name: spring-ai-patterns-guide
description: Spring AI 패턴 가이드. ChatClient 사용, ChatMemoryAdvisor 설정, MCP 도구 정의, AI 응답 처리 시 사용. Spring AI 2.0 기반 개발.
---

# Spring AI 패턴 가이드

Spring AI 2.0 기반 개발 패턴입니다.

## ChatClient 설정

### 기본 ChatClient 빌더

```kotlin
@Configuration
class ChatClientConfig {

    @Bean
    fun chatClient(
        chatClientBuilder: ChatClient.Builder,
        toolCallbackProvider: ToolCallbackProvider,
        chatMemory: ChatMemory
    ): ChatClient {
        return chatClientBuilder
            .defaultSystem("""
                당신은 도움이 되는 AI 어시스턴트입니다.
                사용 가능한 도구를 활용하여 사용자 요청을 처리하세요.
            """.trimIndent())
            .defaultTools(toolCallbackProvider)
            .defaultAdvisors(
                MessageWindowChatMemory(chatMemory, 10)
            )
            .build()
    }
}
```

### Vertex AI Gemini 설정

```yaml
spring:
  ai:
    vertex:
      ai:
        gemini:
          project-id: ${GOOGLE_CLOUD_PROJECT}
          location: ${GOOGLE_CLOUD_LOCATION:us-central1}
          chat:
            options:
              model: gemini-2.0-flash
              temperature: 0.7
```

## ChatMemory 패턴

### MessageWindowChatMemory (권장)

```kotlin
@Bean
fun chatMemory(): ChatMemory {
    return MessageWindowChatMemory.builder()
        .maxMessages(20)  // 최근 20개 메시지 유지
        .build()
}
```

### ChatMemoryAdvisor 적용

```kotlin
// ChatClient에 Advisor로 추가
.defaultAdvisors(
    MessageWindowChatMemory(chatMemory, 10)  // 윈도우 크기 10
)
```

## MCP 도구 정의

### @Tool 어노테이션

```kotlin
@Tool(
    name = "searchProducts",  // 도구 이름 (camelCase)
    description = """
        상품을 검색합니다.
        상품명에 키워드가 포함된 상품 목록을 반환합니다.
        사용 시점: 사용자가 상품 검색을 요청할 때
    """
)
fun searchProducts(
    @ToolParam(description = "검색할 상품명 키워드", required = true)
    keyword: String
): List<ProductInfo> {
    // 구현
}
```

### @ToolParam 어노테이션

```kotlin
@ToolParam(
    description = "파라미터에 대한 상세 설명",
    required = true  // 필수 여부
)
param: String

// 선택적 파라미터 (기본값 제공)
@ToolParam(description = "페이지 크기")
pageSize: Int = 10
```

### ToolCallbackProvider 등록

```kotlin
@Configuration
class ToolConfiguration {

    @Bean
    fun allTools(
        libraryMcpService: LibraryMcpService,
        todoMcpService: TodoMcpService
    ): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(libraryMcpService, todoMcpService)
            .build()
    }
}
```

## ChatClient 호출 패턴

### 기본 호출

```kotlin
val response = chatClient.prompt()
    .user(userMessage)
    .call()
    .content()
```

### 세션 ID로 대화 컨텍스트 유지

```kotlin
val response = chatClient.prompt()
    .user(userMessage)
    .advisors { advisorSpec ->
        advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId)
    }
    .call()
    .content()
```

### 도구 사용 정보 추출

```kotlin
val chatResponse = chatClient.prompt()
    .user(userMessage)
    .call()
    .chatResponse()

// 사용된 도구 목록 추출
val toolsUsed = chatResponse.results
    .flatMap { it.output.toolCalls }
    .map { it.name }
    .distinct()
```

### 스트리밍 응답

```kotlin
val flux: Flux<String> = chatClient.prompt()
    .user(userMessage)
    .stream()
    .content()
```

## MCP 클라이언트 설정

### 여러 MCP 서버 연결

```yaml
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            library-server:
              url: http://localhost:8091
            todo-server:
              url: http://localhost:8096
            employee-server:
              url: http://localhost:8097
            product-server:
              url: http://localhost:8098
```

### SseMcpAsyncClient 빈

```kotlin
@Bean
fun mcpClients(
    @Value("\${mcp.servers.library.url}") libraryUrl: String
): List<McpAsyncClient> {
    return listOf(
        SseMcpAsyncClient.builder(libraryUrl).build()
    )
}
```

## 에러 처리 패턴

### 도구 실행 결과 반환

```kotlin
// 성공/실패를 명시적으로 반환
data class OperationResult(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)

@Tool(name = "createItem")
fun createItem(name: String): OperationResult {
    return try {
        val item = service.create(name)
        OperationResult(true, "생성 완료", item.toInfo())
    } catch (e: Exception) {
        OperationResult(false, "생성 실패: ${e.message}")
    }
}
```

### ChatClient 에러 처리

```kotlin
try {
    val response = chatClient.prompt()
        .user(message)
        .call()
        .content()
} catch (e: Exception) {
    logger.error("AI 응답 생성 실패", e)
    // 폴백 응답 또는 에러 메시지 반환
}
```

## 베스트 프랙티스

### 1. 도구 설명 작성

```kotlin
// BAD - 모호한 설명
description = "데이터를 가져옵니다"

// GOOD - 구체적인 설명
description = """
    사용자의 할일 목록을 조회합니다.
    완료된 항목과 미완료 항목을 모두 포함합니다.
    사용 시점: 사용자가 자신의 할일을 확인하고 싶을 때
"""
```

### 2. 도구 결과 형식

```kotlin
// 간결하고 AI가 이해하기 쉬운 형식
data class BookInfo(
    val id: Long,
    val title: String,
    val author: String,
    val available: Boolean,  // 대출 가능 여부를 명확히
    val status: String       // 상태 문자열
)
```

### 3. 시스템 프롬프트

```kotlin
.defaultSystem("""
    당신은 MCP 서버의 도구들을 활용하는 AI 어시스턴트입니다.

    규칙:
    1. 사용자 요청에 적합한 도구를 선택하여 사용하세요
    2. 도구 실행 결과를 자연스러운 한국어로 설명하세요
    3. 에러 발생 시 사용자에게 명확히 안내하세요

    사용 가능한 도구:
    - searchBooks: 도서 검색
    - borrowBook: 도서 대출
    - getTodoLists: 할일 목록 조회
""".trimIndent())
```

## 의존성 버전

```kotlin
// build.gradle.kts
extra["springAiVersion"] = "2.0.0-M1"

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

dependencies {
    // MCP 서버
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")

    // MCP 클라이언트
    implementation("org.springframework.ai:spring-ai-mcp-client-spring-boot-starter")

    // Vertex AI (Gemini)
    implementation("org.springframework.ai:spring-ai-vertex-ai-gemini-spring-boot-starter")
}
```
