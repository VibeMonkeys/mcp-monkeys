---
name: mcp-server-development-guide
description: MCP 서버 개발 가이드. 새 MCP 서버 생성, MCP 도구 추가, Entity/Repository/Service 구조 설계 시 사용. Spring AI 2.0 기반 MCP 서버 패턴 적용.
---

# MCP 서버 개발 가이드

이 프로젝트의 MCP 서버 개발 표준입니다.

## 프로젝트 구조

새 MCP 서버 모듈을 만들 때 다음 구조를 따릅니다:

```
mcp-{서버명}-server/
├── build.gradle.kts
├── src/main/kotlin/com/monkeys/{서버명}/
│   ├── {서버명}Application.kt      # Spring Boot 메인
│   ├── config/
│   │   └── ToolConfiguration.kt    # MCP 도구 빈 등록
│   ├── entity/
│   │   └── {Entity}.kt             # JPA 엔티티
│   ├── repository/
│   │   └── {Entity}Repository.kt   # Spring Data JPA
│   ├── service/
│   │   ├── {Domain}Service.kt      # 비즈니스 로직
│   │   └── {Domain}McpService.kt   # MCP 도구 정의
│   └── controller/                  # (선택) REST API
└── src/main/resources/
    ├── application.yml
    └── data.sql                     # 초기 데이터
```

## MCP 도구 정의 패턴

### McpService 클래스 구조

```kotlin
@Service
@Transactional(readOnly = true)
class {Domain}McpService(
    private val {domain}Service: {Domain}Service
) {
    private val logger = LoggerFactory.getLogger({Domain}McpService::class.java)

    @Tool(
        name = "도구명",
        description = "도구 설명. AI가 언제 이 도구를 사용할지 명확히 기술."
    )
    fun 도구메서드(
        @ToolParam(description = "파라미터 설명", required = true)
        param: String
    ): ResultType {
        // 1. 입력 검증
        val validated = ValidationUtils.requireNotBlank(param, "파라미터명")

        // 2. 로깅
        logger.info("MCP Tool 호출: 도구명 - param=$validated")

        // 3. 서비스 호출 및 결과 반환
        return {domain}Service.메서드(validated).toInfo()
    }
}
```

### 도구 결과 DTO 패턴

```kotlin
// 단순 정보 반환
data class {Entity}Info(
    val id: Long,
    val name: String,
    // ... 필요한 필드
)

// 작업 결과 반환 (성공/실패 포함)
data class {Action}Result(
    val success: Boolean,
    val message: String,
    val data: {Entity}Info?
)
```

### Extension Function으로 변환

```kotlin
private fun {Entity}.toInfo() = {Entity}Info(
    id = id,
    name = name,
    // ... 매핑
)
```

## build.gradle.kts 필수 의존성

```kotlin
dependencies {
    // Shared Module (ValidationUtils 등)
    implementation(project(":shared"))

    // Spring AI MCP
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Database
    runtimeOnly("com.h2database:h2")
}
```

## ToolConfiguration 설정

```kotlin
@Configuration
class ToolConfiguration {

    @Bean
    fun {domain}Tools({domain}McpService: {Domain}McpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects({domain}McpService)
            .build()
    }
}
```

## application.yml 설정

```yaml
spring:
  application:
    name: mcp-{서버명}-server
  ai:
    mcp:
      server:
        name: {서버명}-mcp-server
        version: 1.0.0
        type: SYNC
  datasource:
    url: jdbc:h2:mem:{서버명}db
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false

server:
  port: 809X  # 고유 포트 할당
```

## 입력 검증 필수 적용

모든 MCP 도구에서 ValidationUtils 사용:

```kotlin
// 필수 문자열
val validated = ValidationUtils.requireNotBlank(input, "필드명")

// 이메일 검증
val email = ValidationUtils.validateEmail(emailInput, "이메일")

// 양수 검증
ValidationUtils.requirePositive(amount, "수량")

// 범위 검증
ValidationUtils.validateRange(minPrice, maxPrice, "최소가격", "최대가격")
```

## 체크리스트

새 MCP 서버 생성 시:

- [ ] settings.gradle.kts에 모듈 추가
- [ ] build.gradle.kts에 shared 모듈 의존성 추가
- [ ] Entity에 JPA 어노테이션 추가
- [ ] Repository에 KotlinJdslJpqlExecutor 상속
- [ ] McpService에 @Tool, @ToolParam 어노테이션 적용
- [ ] ToolConfiguration에 빈 등록
- [ ] 입력 검증(ValidationUtils) 적용
- [ ] 고유 포트 번호 설정
- [ ] data.sql로 초기 데이터 추가
