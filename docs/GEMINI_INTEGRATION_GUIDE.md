# Google Gemini 통합 가이드

## 왜 Google Gemini인데 OpenAI 설정을 사용하는가?

Google은 Gemini API에 **OpenAI 호환성 레이어**를 제공합니다. 이는 개발자들이 기존의 OpenAI 클라이언트 라이브러리와 코드를 최소한의 변경으로 Gemini 모델을 사용할 수 있도록 하는 전략입니다.

### 호환성 레이어의 장점:
1. **코드 재사용성**: 기존 OpenAI 기반 코드를 거의 그대로 사용 가능
2. **빠른 마이그레이션**: API 엔드포인트와 인증 방식만 변경하면 됨
3. **라이브러리 호환성**: Spring AI의 OpenAI 클라이언트를 그대로 활용
4. **표준화된 인터페이스**: OpenAI의 표준 API 형식을 유지
5. **비용 효율성**: Google의 경쟁력 있는 가격 정책
6. **최신 AI 기능**: Gemini 2.0-flash-exp의 고성능 처리 능력

## 전체 설정 과정

### 1. Gradle 의존성 설정 (build.gradle.kts)

```kotlin
dependencies {
    // Spring AI OpenAI Starter (Gemini 호환)
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    
    // MCP 클라이언트 지원
    implementation("org.springframework.ai:spring-ai-mcp-client")
    
    // 기타 의존성들...
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

### 2. application.yml 설정

```yaml
spring:
  application:
    name: unified-mcp-client
  ai:
    openai:
      api-key: ${GEMINI_API_KEY:YOUR_GEMINI_API_KEY_HERE}
      base-url: https://generativelanguage.googleapis.com/v1beta/openai
      chat:
        completions-path: /chat/completions
        options:
          model: gemini-2.0-flash-exp
    mcp:
      client:
        enabled: true
        type: SYNC
        request-timeout: 30s
        sse:
          transport-mode: WEBFLUX
          connections:
            weather:
              url: http://localhost:8092
              name: weather-mcp-server
            news:
              url: http://localhost:8093
              name: news-mcp-server
            translate:
              url: http://localhost:8094
              name: translate-mcp-server
            calendar:
              url: http://localhost:8095
              name: calendar-mcp-server

# MCP 서버 URL 설정
mcp:
  weather:
    url: ${MCP_WEATHER_URL:http://localhost:8092}
  news:
    url: ${MCP_NEWS_URL:http://localhost:8093}
  translate:
    url: ${MCP_TRANSLATE_URL:http://localhost:8094}
  calendar:
    url: ${MCP_CALENDAR_URL:http://localhost:8095}
```

### 3. 환경 변수 설정

필요한 환경 변수들을 설정:
```bash
export GEMINI_API_KEY="YOUR_GEMINI_API_KEY_HERE"
export MCP_WEATHER_URL="http://localhost:8092"
export MCP_NEWS_URL="http://localhost:8093"
export MCP_TRANSLATE_URL="http://localhost:8094"
export MCP_CALENDAR_URL="http://localhost:8095"
```

### 4. Kotlin 설정 클래스

```kotlin
package com.monkeys.client.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeminiConfig {

    @Bean
    fun chatClient(chatModel: OpenAiChatModel): ChatClient {
        return ChatClient.builder(chatModel)
            .defaultSystem("""
당신은 MCP Monkeys의 통합 AI 어시스턴트입니다.

사용 가능한 서비스들:
- 🌤️ Weather: 날씨 정보 조회
- 📰 News: 뉴스 검색 및 조회  
- 🌐 Translate: 텍스트 번역
- 📅 Calendar: 일정 관리

주요 특징:
- 여러 서비스를 연계한 복합적인 작업 수행 가능
- 대화 맥락을 기억하여 연속적인 대화 지원
- 실시간 정보 제공 및 업데이트

사용자의 요청을 정확히 파악하고 적절한 도구를 선택하여 도움이 되는 응답을 제공해주세요.
            """.trimIndent())
            .build()
    }
}
```

## 핵심 설정 요소 설명

### 1. Base URL 변경
```yaml
base-url: https://generativelanguage.googleapis.com/v1beta/openai
```
- Google의 OpenAI 호환 엔드포인트를 지정
- OpenAI 대신 Google 서버로 요청을 라우팅

### 2. API 키 환경 변수
```yaml
api-key: ${GEMINI_API_KEY:YOUR_GEMINI_API_KEY_HERE}
```
- Gemini API 키를 사용하되 OpenAI 클라이언트 형식으로 전달
- 환경 변수를 통한 보안 관리

### 3. 모델 지정
```yaml
model: gemini-2.0-flash-exp
```
- Gemini의 최신 실험 모델 사용
- OpenAI 모델명 대신 Gemini 모델명 지정

### 4. 시스템 프롬프트 설정
GeminiConfig에서 defaultSystem을 통해 AI 어시스턴트의 역할과 기능을 정의:
- MCP Monkeys 프로젝트의 통합 AI 어시스턴트로 설정
- 사용 가능한 서비스들(Weather, News, Translate, Calendar) 명시
- 다중 서비스 연계 및 대화 맥락 지원 기능 안내

## 테스트 방법

### 1. 기본 연결 테스트
```bash
curl -X GET http://localhost:8090/api/health
```

### 2. MCP 서버 상태 확인
```bash
curl -X GET http://localhost:8090/api/health/mcp-servers
```

### 3. 종합 헬스 체크
```bash
curl -X GET http://localhost:8090/api/health/comprehensive
```

### 4. 채팅 테스트 (기본)
```bash
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "안녕하세요, Gemini!"}'
```

### 5. 도구 활용 채팅 테스트
```bash
# 날씨 조회
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "서울 날씨 어때?", "tools": ["getWeather"]}'

# 뉴스 검색
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "최신 기술 뉴스 알려줘", "tools": ["searchNews"]}'
```

## 프로젝트 구조와 통합

### MCP 서버 연동
- **Weather Server**: 날씨 정보 API 연동 (포트: 8092)
- **News Server**: 뉴스 검색 API 연동 (포트: 8093)
- **Translate Server**: 번역 서비스 연동 (포트: 8094)
- **Calendar Server**: 일정 관리 서비스 연동 (포트: 8095)

### 프론트엔드 연동
- React/TypeScript 기반 웹 인터페이스
- Vite 개발 서버 (포트: 5173)
- MCP API 서비스 레이어를 통한 백엔드 연동

## 주요 이점

1. **개발 효율성**: 기존 OpenAI 코드 재활용
2. **성능**: Gemini 2.0-flash-exp의 빠른 응답 속도
3. **비용 효율성**: Google의 경쟁력 있는 가격 정책
4. **최신 기능**: Gemini의 멀티모달 및 고급 기능 활용
5. **통합성**: MCP 프로토콜을 통한 다중 서비스 연계
6. **모니터링**: Prometheus 메트릭 및 헬스 체크 지원

## 주의사항

- API 키는 절대 소스코드에 하드코딩하지 말고 환경 변수 사용
- Gemini 모델의 사용량 제한 및 요금 체계 확인
- OpenAI와 다른 응답 형식이나 제한사항이 있을 수 있음
- MCP 서버들이 정상 실행 중인지 확인 필요
- Circuit Breaker 패턴을 통한 장애 대응 고려

## 실행 순서

1. **환경 변수 설정**: GEMINI_API_KEY 등 필수 환경 변수 설정
2. **MCP 서버들 실행**: 각 포트별로 서버 실행
3. **클라이언트 실행**: 통합 MCP 클라이언트 실행 (포트: 8090)
4. **프론트엔드 실행**: 웹 인터페이스 실행 (포트: 5173)
5. **테스트**: API 엔드포인트를 통한 기능 검증

이러한 방식으로 Google Gemini를 OpenAI 클라이언트를 통해 사용함으로써 최소한의 코드 변경으로 최대한의 효과를 얻을 수 있으며, MCP 프로토콜을 통한 다양한 서비스들의 통합된 AI 어시스턴트를 구축할 수 있습니다.