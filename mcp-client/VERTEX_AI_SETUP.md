# 🚀 Vertex AI Gemini 설정 가이드

MCP Monkeys 프로젝트에서 Google Vertex AI Gemini를 사용하기 위한 완전한 설정 가이드입니다.

## 📋 **전체 개요**

이 가이드는 Spring AI 1.0.1과 Vertex AI Gemini 2.5-flash를 사용하여 MCP Client를 설정하는 방법을 다룹니다.

## 🎯 **필요 사항**

- Google Cloud 계정 (Gmail 계정으로 가능)
- Google Cloud CLI (gcloud)
- 프로젝트 ID: `gen-lang-client-0124812904(예시: 직접 구글 클라우드 프로젝트에서 확인하기)`

## 1️⃣ **Google Cloud CLI 설치**

### macOS (Homebrew)
```bash
brew install google-cloud-sdk
```

### Linux/Windows
[Google Cloud CLI 설치 페이지](https://cloud.google.com/sdk/docs/install)에서 다운로드

## 2️⃣ **Google Cloud 인증 설정**

### 기본 인증
```bash
# 1. Google 계정으로 로그인 (브라우저 열림)
gcloud auth login

# 2. Application Default Credentials 설정 (중요!)
gcloud auth application-default login
# ⚠️ 브라우저가 열리면 **추가 권한 2개를 반드시 승인**해야 합니다!
# - Cloud Platform (전체 액세스)
# - Vertex AI API 액세스

# 3. 프로젝트 설정
gcloud config set project {내 프로젝트 ID 넣기}
```

### 인증 확인
```bash
# 현재 인증 상태 확인
gcloud auth list

# 프로젝트 확인
gcloud config get-value project
```

## 3️⃣ **Vertex AI API 활성화**

```bash
# Vertex AI API 활성화
gcloud services enable aiplatform.googleapis.com

# 활성화된 서비스 확인
gcloud services list --enabled | grep aiplatform
```

## 4️⃣ **프로젝트 설정 확인**

### build.gradle.kts
```kotlin
dependencies {
    implementation("org.springframework.ai:spring-ai-starter-model-vertex-ai-gemini")
    // ... 기타 의존성
}
```

### application.yml
```yaml
spring:
  application:
    name: unified-mcp-client
  ai:
    # Vertex AI는 Google Cloud 인증을 사용하므로 별도 API 키가 필요 없습니다.
    vertex:
      ai:
        gemini:
          project-id: {프로젝트 id 넣기}
          location: ${GOOGLE_CLOUD_LOCATION:asia-northeast1} # 도쿄가 한국에서는 빠름
          chat:
            options:
              model: gemini-1.5-flash
              temperature: 0.7
```

### Config 클래스 (MCP 도구 통합 포함)
```kotlin
package com.monkeys.client.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.model.tool.ToolCallbackProvider
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VertexAIConfig(
    private val toolCallbackProvider: ToolCallbackProvider  // MCP 도구들이 자동 주입됨
) {

    @Bean
    fun chatClient(chatModel: VertexAiGeminiChatModel): ChatClient {
        val toolCallbacks = toolCallbackProvider.getToolCallbacks()
        println("=== MCP 도구 등록 상황 ===")
        println("등록된 도구 수: ${toolCallbacks.size}")
        toolCallbacks.forEach { tool ->
            println("도구 클래스: ${tool.javaClass}")
            println("도구: $tool")
        }
        println("========================")
        
        return ChatClient.builder(chatModel)
            .defaultToolCallbacks(*toolCallbacks)  // MCP 도구들을 ChatClient에 등록
            .defaultSystem("""
당신은 MCP Monkeys의 통합 AI 어시스턴트입니다.

사용 가능한 실제 도구들:
- 🌤️ Weather Tools: getCurrentWeather, getWeatherForecast, compareWeather
- 📰 News Tools: getTopHeadlines, searchNews, getNewsByCategory  
- 🌐 Translate Tools: translateText, detectLanguage, getSupportedLanguages, batchTranslate
- 📅 Calendar Tools: createEvent, getEvents, deleteEvent, getCalendars

⚠️ 중요한 동작 방식:
- 사용자가 날씨를 물으면 getCurrentWeather 도구를 호출하여 실제 데이터를 가져옵니다
- 번역 요청 시 translateText 도구를 실제로 호출합니다
- 뉴스 검색 시 실제 News API를 통해 최신 정보를 가져옵니다
- 일정 관리 시 실제 캘린더 시스템과 연동됩니다

사용자의 요청을 정확히 파악하고 적절한 도구를 선택하여 실제 데이터를 기반으로 도움이 되는 응답을 제공해주세요.
            """.trimIndent())
            .build()
    }
}
```

## 5️⃣ **환경변수 설정 (실제 배포시 선택사항)**

### 터미널에서 실행
```bash
export GOOGLE_CLOUD_PROJECT="gen-lang-client-0124812904"
export GOOGLE_CLOUD_LOCATION="asia-northeast1"
```

### 영구 설정 (선택사항)
```bash
# ~/.zshrc 또는 ~/.bashrc에 추가
echo 'export GOOGLE_CLOUD_PROJECT="gen-lang-client-0124812904"' >> ~/.zshrc
echo 'export GOOGLE_CLOUD_LOCATION="asia-northeast1"' >> ~/.zshrc
source ~/.zshrc
```

## 🔧 **MCP 도구 등록 설정 (중요!)**

### ⚠️ MCP 서버에서 도구 등록 필수 단계

각 MCP 서버 (weather, news, translate, calendar)에서 도구가 실제로 AI에 의해 호출되려면 **`ToolCallbackProvider` 빈 등록이 필수**입니다:

```kotlin
// 예: WeatherMcpServerApplication.kt
@SpringBootApplication
class WeatherMcpServerApplication {

    // 🎯 이 빈이 없으면 도구가 "Registered tools: 0"으로 등록되지 않습니다!
    @Bean
    fun weatherTools(weatherMcpService: WeatherMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(weatherMcpService)
            .build()
    }
}
```

### 도구 등록 확인 방법

MCP 서버 시작 시 다음 로그를 확인하세요:

```bash
✅ 성공적인 등록:
2025-09-11T13:08:29.020+09:00  INFO [...] o.s.a.m.s.a.McpServerAutoConfiguration : Registered tools: 3

❌ 실패한 등록:  
2025-09-11T13:08:29.020+09:00  INFO [...] o.s.a.m.s.a.McpServerAutoConfiguration : Registered tools: 0
```

**📚 자세한 내용**: 루트 디렉토리의 [`MCP_TOOL_REGISTRATION_GUIDE.md`](../MCP_TOOL_REGISTRATION_GUIDE.md) 참조

## 6️⃣ **서버 실행**

### 편리한 실행 방법 (추천)
```bash
# 1. 모든 서버를 한 번에 시작
./start-all-servers.sh

# 2. 모든 서버를 한 번에 종료  
./stop-all-servers.sh
```

### 수동 실행 방법
```bash
# 1. 모든 MCP 서버 시작
./gradlew :mcp-weather-server:bootRun --args='--server.port=8092' &
./gradlew :mcp-news-server:bootRun --args='--server.port=8093' &
./gradlew :mcp-translate-server:bootRun --args='--server.port=8094' &
./gradlew :mcp-calendar-server:bootRun --args='--server.port=8095' &
```

### MCP Client 시작
```bash
# 2. 환경변수와 함께 클라이언트 실행
export GOOGLE_CLOUD_PROJECT="gen-lang-client-0311835119" && \
export GOOGLE_CLOUD_LOCATION="asia-northeast1" && \
./gradlew :mcp-client:bootRun --args='--server.port=8090'
```

## 7️⃣ **연결 확인**

### 🔍 MCP 도구 등록 확인

**MCP 클라이언트 시작 시 다음 로그를 확인하세요:**

```
✅ 성공적인 MCP 도구 등록:
=== MCP 도구 등록 상황 ===
등록된 도구 수: 14
도구 클래스: class org.springframework.ai.mcp.SyncMcpToolCallback
...
========================
```

### 성공적인 시작 로그 예시
```
2025-09-11T13:09:07.367+09:00  INFO 541 --- [unified-mcp-client] [           main] c.monkeys.client.McpClientApplicationKt  : Starting McpClientApplicationKt
2025-09-11T13:09:09.975+09:00  INFO 541 --- [unified-mcp-client] [ctor-http-nio-2] i.m.client.McpAsyncClient : Server response with Protocol: 2024-11-05, Capabilities: ServerCapabilities[tools=ToolCapabilities[listChanged=true]], Info: Implementation[name=weather-mcp-server, version=1.0.0]
2025-09-11T13:09:10.131+09:00  INFO 541 --- [unified-mcp-client] [ctor-http-nio-3] i.m.client.McpAsyncClient : Server response with Protocol: 2024-11-05, Capabilities: ServerCapabilities[tools=ToolCapabilities[listChanged=true]], Info: Implementation[name=news-mcp-server, version=1.0.0]
2025-09-11T13:09:10.293+09:00  INFO 541 --- [unified-mcp-client] [ctor-http-nio-4] i.m.client.McpAsyncClient : Server response with Protocol: 2024-11-05, Capabilities: ServerCapabilities[tools=ToolCapabilities[listChanged=true]], Info: Implementation[name=calendar-mcp-server, version=1.0.0]
2025-09-11T13:09:10.454+09:00  INFO 541 --- [unified-mcp-client] [ctor-http-nio-5] i.m.client.McpAsyncClient : Server response with Protocol: 2024-11-05, Capabilities: ServerCapabilities[tools=ToolCapabilities[listChanged=true]], Info: Implementation[name=translate-mcp-server, version=1.0.0]
```

### 🔧 실제 도구 호출 확인

**MCP 도구가 AI에 의해 실제로 호출되는 로그:**

```
✅ 실제 도구 호출:
2025-09-11T13:09:48.209+09:00 DEBUG 541 --- o.s.a.m.tool.DefaultToolCallingManager : Executing tool call: spring_ai_mcp_client_weather_getCurrentWeather
2025-09-11T13:09:58.744+09:00 DEBUG 541 --- o.s.a.m.tool.DefaultToolCallingManager : Executing tool call: spring_ai_mcp_client_news_getTopHeadlines
2025-09-11T13:10:05.365+09:00 DEBUG 541 --- o.s.a.m.tool.DefaultToolCallingManager : Executing tool call: spring_ai_mcp_client_translate_translateText
```

## 8️⃣ **API 테스트**

### 기본 헬스체크
```bash
curl http://localhost:8090/actuator/health
```

### MCP 서버 연결 상태
```bash
curl http://localhost:8090/api/health/mcp-servers
```

### 채팅 API 테스트
```bash
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "안녕하세요! 오늘 서울 날씨 어때요?",
    "sessionId": "test-session"
  }'
```

## 🔧 **트러블슈팅**

### 인증 오류
```bash
# 인증 재설정
gcloud auth revoke --all
gcloud auth login
gcloud auth application-default login
```

### 프로젝트 ID 확인
```bash
# 현재 설정된 프로젝트 확인
gcloud config get-value project

# 프로젝트 변경
gcloud config set project gen-lang-client-0311835119
```

### API 활성화 확인
```bash
# Vertex AI API 상태 확인
gcloud services list --enabled | grep aiplatform

# 수동 활성화 (필요시)
gcloud services enable aiplatform.googleapis.com
```

## 📊 **지원 모델 및 리전**

### 지원 모델
- `gemini-1.5-flash` (현재 설정, 안정적)
- `gemini-2.0-flash` 
- `gemini-2.5-flash`

### 권장 리전 (한국 기준)
1. `asia-northeast1` (도쿄) - **추천**: 낮은 지연시간
2. `us-central1` (미국 중부) - 안정성 높음
3. `europe-west4` (네덜란드) - 유럽 사용자

## 🎯 **주요 장점**

✅ **API 키 불필요**: Google Cloud 인증 사용  
✅ **높은 성능**: Gemini 2.5-flash 모델  
✅ **Tool 지원**: Spring AI MCP 통합  
✅ **한국 최적화**: asia-northeast1 리전  
✅ **비용 효율**: 경쟁력 있는 가격  

## 🔗 **관련 문서**

- [Google Cloud CLI 설치](https://cloud.google.com/sdk/docs/install)
- [Vertex AI 문서](https://cloud.google.com/vertex-ai/docs)
- [Spring AI Vertex AI 가이드](https://docs.spring.io/spring-ai/reference/api/chat/vertexai-gemini-chat.html)
- [MCP Monkeys 메인 README](../README.md)

---

## 🎯 **중요한 권한 승인 과정**

`gcloud auth application-default login` 실행 시 브라우저가 열리면:

1. **Google 계정 로그인**
2. **권한 승인 화면**에서 반드시 다음 2개 권한을 **모두 승인**:
   - ✅ **Google Cloud Platform 전체 액세스** 
   - ✅ **Vertex AI API 액세스**
3. 모든 권한을 승인해야 `"Credentials saved to file: [/Users/username/.config/gcloud/application_default_credentials.json]"` 메시지 확인

⚠️ **권한을 부분적으로만 승인하면 "Failed to generate content" 오류 발생**

**💡 Tips**: 권한 승인을 제대로 하지 않았다면 `gcloud auth application-default login`을 다시 실행하여 모든 권한을 승인하세요.