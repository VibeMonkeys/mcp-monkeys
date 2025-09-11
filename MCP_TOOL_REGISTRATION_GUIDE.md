# 🔧 MCP 도구 등록 완전 가이드

Spring AI 1.0.1에서 MCP (Model Context Protocol) 도구를 올바르게 등록하는 방법에 대한 완전한 가이드입니다.

## 📋 **개요**

MCP 도구가 AI에 의해 실제로 호출되려면 단순히 `@Tool` 어노테이션만으로는 충분하지 않습니다. **Spring AI MCP 프로토콜에 맞는 `ToolCallbackProvider` 빈 등록이 필수**입니다.

## ⚠️ **중요: 반드시 필요한 단계**

### 1. **@Tool 어노테이션으로 도구 정의**
### 2. **ToolCallbackProvider 빈 등록** ← **이것이 핵심!**

## 🛠️ **Step 1: 도구 서비스 클래스 작성**

```kotlin
@Service
class WeatherMcpService(
    @Value("\${weather.api.key:dummy-key}") private val apiKey: String,
    private val weatherHttpClient: OkHttpClient,
    private val meterRegistry: MeterRegistry
) {
    @Tool(description = "지정된 도시의 현재 날씨 정보를 조회합니다")
    fun getCurrentWeather(
        @ToolParam(description = "도시 이름 (예: Seoul, Tokyo, New York)", required = true)
        city: String,
        @ToolParam(description = "단위 (metric: 섭씨, imperial: 화씨, kelvin: 켈빈)")
        units: String = "metric"
    ): WeatherInfo {
        // 도구 구현 로직
        return WeatherInfo(...)
    }

    @Tool(description = "지정된 도시의 5일 날씨 예보를 조회합니다")
    fun getWeatherForecast(
        @ToolParam(description = "도시 이름", required = true)
        city: String,
        @ToolParam(description = "단위")
        units: String = "metric"
    ): List<WeatherForecast> {
        // 예보 구현 로직
        return listOf(...)
    }
}
```

## 🔑 **Step 2: ToolCallbackProvider 빈 등록 (핵심!)**

**메인 애플리케이션 클래스에 반드시 추가해야 하는 빈:**

```kotlin
package com.monkeys.weather

import com.monkeys.weather.service.WeatherMcpService
import org.springframework.ai.model.tool.MethodToolCallbackProvider
import org.springframework.ai.model.tool.ToolCallbackProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class WeatherMcpServerApplication {

    // 🎯 이 빈이 없으면 도구가 등록되지 않습니다!
    @Bean
    fun weatherTools(weatherMcpService: WeatherMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(weatherMcpService)
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<WeatherMcpServerApplication>(*args)
}
```

## 📊 **Step 3: MCP 서버 설정**

**application.yml에 MCP 서버 설정 추가:**

```yaml
spring:
  application:
    name: weather-mcp-server
  ai:
    mcp:
      server:
        enabled: true
        name: weather-mcp-server
        version: 1.0.0
        type: SYNC
        description: "Weather information service using OpenWeatherMap API"
        sse-message-endpoint: /mcp/messages
        capabilities:
          tool: true
          resource: false
          prompt: false
          completion: true
```

## 🔍 **Step 4: 도구 등록 확인**

**서버 시작 시 다음 로그를 확인하세요:**

```
✅ 성공적인 도구 등록:
2025-09-11T13:08:29.020+09:00  INFO [weather-mcp-server] o.s.a.m.s.a.McpServerAutoConfiguration   : Registered tools: 3

❌ 실패한 도구 등록:
2025-09-11T13:08:29.020+09:00  INFO [weather-mcp-server] o.s.a.m.s.a.McpServerAutoConfiguration   : Registered tools: 0
```

## 🔧 **전체 프로젝트 구조**

```
mcp-weather-server/
├── src/main/kotlin/com/monkeys/weather/
│   ├── WeatherMcpServerApplication.kt    ← ToolCallbackProvider 빈 등록
│   ├── service/
│   │   └── WeatherMcpService.kt          ← @Tool 어노테이션
│   └── config/
│       └── WeatherConfig.kt
├── src/main/resources/
│   └── application.yml                   ← MCP 서버 설정
└── build.gradle.kts                      ← 의존성
```

## 📋 **체크리스트**

도구가 제대로 등록되지 않는다면 다음을 확인하세요:

- [ ] `@Tool` 어노테이션이 서비스 메서드에 있는가?
- [ ] **`ToolCallbackProvider` 빈이 메인 애플리케이션 클래스에 등록되어 있는가?** (가장 중요!)
- [ ] `application.yml`에 MCP 서버 설정이 있는가?
- [ ] `build.gradle.kts`에 `spring-ai-starter-mcp-server-webmvc` 의존성이 있는가?
- [ ] 서버 시작 시 "Registered tools: X" 로그에서 X > 0인가?

## 🚨 **일반적인 실수들**

### ❌ **실수 1: ToolCallbackProvider 빈 누락**
```kotlin
// 이것만으로는 충분하지 않습니다!
@SpringBootApplication
class WeatherMcpServerApplication  // 빈 등록 없음

// ❌ 결과: Registered tools: 0
```

### ✅ **올바른 방법: ToolCallbackProvider 빈 등록**
```kotlin
@SpringBootApplication
class WeatherMcpServerApplication {
    @Bean
    fun weatherTools(weatherMcpService: WeatherMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(weatherMcpService)
            .build()
    }
}
// ✅ 결과: Registered tools: 3
```

### ❌ **실수 2: 잘못된 의존성**
```kotlin
// 이런 의존성은 MCP 서버용이 아닙니다
implementation("org.springframework.ai:spring-ai-starter-model-vertex-ai-gemini")
```

### ✅ **올바른 의존성: MCP 서버용**
```kotlin
// MCP 서버를 만들 때 필요한 의존성
implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")
```

## 🎯 **MCP Client에서 도구 등록 확인**

**MCP 클라이언트 시작 시 다음 로그를 확인하세요:**

```
✅ 성공적인 연결:
2025-09-11T13:09:09.975+09:00  INFO [unified-mcp-client] i.m.client.McpAsyncClient : Server response with Protocol: 2024-11-05, Capabilities: ServerCapabilities[tools=ToolCapabilities[listChanged=true]], Info: Implementation[name=weather-mcp-server, version=1.0.0]

=== MCP 도구 등록 상황 ===
등록된 도구 수: 14  ← 모든 서버의 도구 합계
```

## 🔍 **실제 도구 호출 확인**

**MCP 클라이언트에서 도구가 실제로 호출되는지 확인:**

```
✅ 도구 호출 성공:
2025-09-11T13:09:48.209+09:00 DEBUG [unified-mcp-client] o.s.a.m.tool.DefaultToolCallingManager : Executing tool call: spring_ai_mcp_client_weather_getCurrentWeather
```

## 📚 **추가 리소스**

- [Spring AI MCP 서버 문서](https://docs.spring.io/spring-ai/reference/api/mcp/)
- [MCP 프로토콜 사양](https://spec.modelcontextprotocol.io/)
- [ToolCallbackProvider API 문서](https://docs.spring.io/spring-ai/reference/api/tool-calling/)

---

## 💡 **핵심 포인트**

**`@Tool` 어노테이션 + `ToolCallbackProvider` 빈 = 실제 동작하는 MCP 도구**

이 두 가지가 모두 있어야만 AI가 실제로 도구를 호출할 수 있습니다!