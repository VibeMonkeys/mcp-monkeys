# Google Gemini 통합 가이드

## 왜 Google Gemini인데 OpenAI 설정을 사용하는가?

Google은 Gemini API에 **OpenAI 호환성 레이어**를 제공합니다. 이는 개발자들이 기존의 OpenAI 클라이언트 라이브러리와 코드를 최소한의 변경으로 Gemini 모델을 사용할 수 있도록 하는 전략입니다.

### 호환성 레이어의 장점:
1. **코드 재사용성**: 기존 OpenAI 기반 코드를 거의 그대로 사용 가능
2. **빠른 마이그레이션**: API 엔드포인트와 인증 방식만 변경하면 됨
3. **라이브러리 호환성**: Spring AI의 OpenAI 클라이언트를 그대로 활용
4. **표준화된 인터페이스**: OpenAI의 표준 API 형식을 유지

## 전체 설정 과정

### 1. Gradle 의존성 설정 (build.gradle.kts)

```kotlin
dependencies {
    // Spring AI OpenAI Starter (Gemini 호환)
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    
    // 기타 의존성들...
}
```

### 2. application.yml 설정

```yaml
spring:
  ai:
    openai:
      api-key: ${GEMINI_API_KEY:YOUR_GEMINI_API_KEY_HERE}
      base-url: https://generativelanguage.googleapis.com/v1beta/openai
      chat:
        completions-path: /chat/completions
        options:
          model: gemini-2.0-flash-exp
```

### 3. 환경 변수 설정

API 키를 환경 변수로 설정:
```bash
export GEMINI_API_KEY="YOUR_GEMINI_API_KEY_HERE"
```

### 4. Kotlin 설정 클래스

```kotlin
package com.monkeys.client.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeminiConfig {

    @Bean
    fun chatClient(builder: ChatClient.Builder): ChatClient {
        return builder.build()
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

## 테스트 방법

### 1. 연결 테스트
```bash
curl -X GET http://localhost:8090/api/health
```

### 2. 채팅 테스트
```bash
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "안녕하세요, Gemini!"}'
```

## 주요 이점

1. **개발 효율성**: 기존 OpenAI 코드 재활용
2. **성능**: Gemini 2.0-flash-exp의 빠른 응답 속도
3. **비용 효율성**: Google의 경쟁력 있는 가격 정책
4. **최신 기능**: Gemini의 멀티모달 및 고급 기능 활용

## 주의사항

- API 키는 절대 소스코드에 하드코딩하지 말고 환경 변수 사용
- Gemini 모델의 사용량 제한 및 요금 체계 확인
- OpenAI와 다른 응답 형식이나 제한사항이 있을 수 있음

이러한 방식으로 Google Gemini를 OpenAI 클라이언트를 통해 사용함으로써 최소한의 코드 변경으로 최대한의 효과를 얻을 수 있습니다.