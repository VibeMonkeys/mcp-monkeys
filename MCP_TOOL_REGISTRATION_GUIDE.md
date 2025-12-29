# MCP 도구 등록 가이드

Spring AI 2.0.0-M1에서 MCP (Model Context Protocol) 도구를 등록하는 방법입니다.

## 개요

MCP 도구가 AI에 의해 호출되려면 두 가지가 필요합니다:

1. `@Tool` 어노테이션으로 도구 정의
2. `ToolCallbackProvider` 빈 등록

## Step 1: 도구 서비스 클래스 작성

```kotlin
@Service
@Transactional(readOnly = true)
class LibraryMcpService(
    private val libraryService: LibraryService
) {
    @Tool(description = "도서를 검색합니다. 제목, 저자명, ISBN으로 검색 가능합니다.")
    fun searchBooks(
        @ToolParam(description = "검색 키워드 (제목, 저자명, ISBN)")
        keyword: String
    ): List<BookInfo> {
        return libraryService.searchBooks(keyword).map { it.toInfo() }
    }

    @Tool(description = "도서를 대출합니다.")
    @Transactional
    fun borrowBook(
        @ToolParam(description = "도서 ISBN") isbn: String,
        @ToolParam(description = "대출자 이름") borrowerName: String,
        @ToolParam(description = "대출자 이메일") borrowerEmail: String
    ): LoanResult {
        val book = libraryService.findByIsbn(isbn)
            ?: return LoanResult(false, "해당 ISBN의 도서를 찾을 수 없습니다: $isbn")

        val loan = libraryService.borrowBook(book.id, borrowerName, borrowerEmail)
            ?: return LoanResult(false, "대출에 실패했습니다. 재고가 없습니다.")

        return LoanResult(true, "대출 성공", loan.toInfo())
    }
}
```

## Step 2: ToolCallbackProvider 빈 등록

**별도 설정 클래스로 분리 권장:**

```kotlin
package com.monkeys.library.config

import com.monkeys.library.service.LibraryMcpService
import org.springframework.ai.tool.MethodToolCallbackProvider
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfiguration {

    @Bean
    fun libraryTools(libraryMcpService: LibraryMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(libraryMcpService)
            .build()
    }
}
```

## Step 3: MCP 서버 설정

**application.yml:**

```yaml
spring:
  application:
    name: library-mcp-server

  ai:
    mcp:
      server:
        enabled: true
        name: library-mcp-server
        version: 1.0.0
        type: SYNC
        sse-message-endpoint: /mcp/messages
        capabilities:
          tool: true
          resource: false
          prompt: false
          completion: true
```

## Step 4: 도구 등록 확인

서버 시작 시 로그 확인:

```
# 성공
INFO [library-mcp-server] o.s.a.m.s.a.McpServerAutoConfiguration : Registered tools: 15

# 실패 (ToolCallbackProvider 누락)
INFO [library-mcp-server] o.s.a.m.s.a.McpServerAutoConfiguration : Registered tools: 0
```

## 프로젝트 구조

```
mcp-library-server/
├── src/main/kotlin/com/monkeys/library/
│   ├── LibraryServerApplication.kt
│   ├── config/
│   │   └── ToolConfiguration.kt       ← ToolCallbackProvider 빈 등록
│   ├── entity/
│   │   ├── Book.kt
│   │   ├── Author.kt
│   │   └── Loan.kt
│   ├── repository/
│   │   ├── BookRepository.kt
│   │   ├── AuthorRepository.kt
│   │   └── LoanRepository.kt
│   └── service/
│       ├── LibraryService.kt          ← 비즈니스 로직
│       └── LibraryMcpService.kt       ← @Tool 어노테이션
└── src/main/resources/
    ├── application.yml                ← MCP 서버 설정
    ├── schema.sql                     ← DDL
    └── data.sql                       ← 초기 데이터
```

## 체크리스트

- [ ] `@Tool` 어노테이션이 서비스 메서드에 있는가?
- [ ] `ToolCallbackProvider` 빈이 등록되어 있는가?
- [ ] `application.yml`에 MCP 서버 설정이 있는가?
- [ ] `build.gradle.kts`에 `spring-ai-starter-mcp-server-webmvc` 의존성이 있는가?
- [ ] 서버 시작 시 "Registered tools: X"에서 X > 0인가?

## 일반적인 실수

### 실수 1: ToolCallbackProvider 빈 누락

```kotlin
// 이것만으로는 충분하지 않습니다!
@SpringBootApplication
class LibraryServerApplication
// 결과: Registered tools: 0
```

### 올바른 방법

```kotlin
@Configuration
class ToolConfiguration {
    @Bean
    fun libraryTools(libraryMcpService: LibraryMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(libraryMcpService)
            .build()
    }
}
// 결과: Registered tools: 15
```

### 실수 2: 트랜잭션 누락

```kotlin
// LazyInitializationException 발생 가능
@Tool(description = "...")
fun getBookWithAuthor(isbn: String): BookInfo {
    val book = libraryService.findByIsbn(isbn)
    return book.toInfo() // author 접근 시 에러
}
```

### 올바른 방법

```kotlin
@Service
@Transactional(readOnly = true) // 클래스 레벨 트랜잭션
class LibraryMcpService(...) {

    @Tool(description = "...")
    fun getBookWithAuthor(isbn: String): BookInfo {
        val book = libraryService.findByIsbn(isbn)
        return book.toInfo() // 트랜잭션 내에서 안전하게 접근
    }
}
```

## MCP Client에서 도구 확인

클라이언트 시작 시 로그:

```
INFO [unified-mcp-client] i.m.client.McpAsyncClient : Server response with Protocol: 2024-11-05, Capabilities: ServerCapabilities[tools=ToolCapabilities[listChanged=true]], Info: Implementation[name=library-mcp-server, version=1.0.0]

=== MCP 도구 등록 상황 ===
등록된 도구 수: 58  ← 모든 서버의 도구 합계
```

## 핵심 포인트

**`@Tool` 어노테이션 + `ToolCallbackProvider` 빈 + `@Transactional` = 동작하는 MCP 도구**

이 세 가지가 모두 있어야 AI가 도구를 안전하게 호출할 수 있습니다.
