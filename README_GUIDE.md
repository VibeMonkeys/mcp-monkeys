# 🚀 MCP Monkeys - Spring AI 통합 하이브리드 AI 어시스턴트 시스템

Spring AI 1.0.1과 Model Context Protocol(MCP)을 활용한 통합 AI 어시스턴트 플랫폼입니다.
Direct API 호출과 MCP 서버 프로토콜을 동시에 지원하는 하이브리드 아키텍처로 구현되었습니다.

## 📋 목차

1. [시스템 개요](#-시스템-개요)
2. [구현된 기능](#-구현된-기능)
3. [시스템 아키텍처](#-시스템-아키텍처)
4. [설치 및 실행](#-설치-및-실행)
5. [API 키 설정](#-api-키-설정)
6. [사용법](#-사용법)
7. [API 엔드포인트](#-api-엔드포인트)
8. [문제 해결](#-문제-해결)

## 🌟 시스템 개요

### 핵심 특징
- **하이브리드 아키텍처**: Direct API + MCP 프로토콜 동시 지원
- **Spring AI 1.0.1 완전 활용**: @Tool 어노테이션, ChatClient 통합
- **8개 통합 도구**: 업무 자동화 및 정보 검색
- **모듈화된 구조**: 각 서비스별 독립적인 MCP 서버
- **보안 강화**: 환경변수 기반 API 키 관리

### 기술 스택
- **Backend**: Spring Boot 3.5.4, Kotlin 1.9.25
- **AI Framework**: Spring AI 1.0.1
- **Protocol**: Model Context Protocol (MCP)
- **Build Tool**: Gradle 8.14
- **Database**: H2 (인메모리)
- **JDK**: OpenJDK 21

## 🛠 구현된 기능

### 1. Direct API 통합 도구 (4개)
**포트 8090 - MCP Client에서 직접 호출**

#### 🐙 GitHub 연동
- `getGitHubIssues`: GitHub 저장소의 이슈 목록 조회
- `createGitHubIssue`: GitHub 저장소에 새로운 이슈 생성

#### 📋 Jira 연동  
- `getJiraIssues`: Jira 프로젝트의 이슈 목록 조회
- `createJiraIssue`: Jira에 새로운 이슈 생성

#### 📧 Gmail 연동
- `getGmailMessages`: Gmail 받은편지함의 메일 목록 조회
- `sendGmailMessage`: Gmail로 메일 발송

#### 💬 Slack 연동
- `sendSlackMessage`: Slack 채널에 메시지 전송
- `getSlackMessages`: Slack 채널의 최근 메시지 조회

### 2. MCP 서버 도구 (4개)
**포트 8092-8095 - 독립적인 MCP 서버들**

#### 🌤️ Weather 서버 (포트 8092)
- `getCurrentWeather`: 지정된 도시의 현재 날씨 정보 조회
- `getWeatherForecast`: 5일 일기예보 제공
- `compareWeather`: 여러 도시 날씨 비교

#### 📰 News 서버 (포트 8093)
- `getTopHeadlines`: 주요 헤드라인 뉴스 조회
- `searchNews`: 키워드 기반 뉴스 검색
- `getNewsBySource`: 특정 뉴스 소스별 기사 조회

#### 🌐 Translate 서버 (포트 8094)
- `translateText`: 텍스트 번역 (자동 언어 감지)
- `detectLanguage`: 입력 텍스트 언어 감지
- `getSupportedLanguages`: 지원 언어 목록 조회

#### 📅 Calendar 서버 (포트 8095)
- `createCalendarEvent`: 새로운 캘린더 이벤트 생성

### 3. 시스템 관리 도구
- `checkAllApiStatus`: 모든 API 연동 상태 확인

## 🏗 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    AI Chat Interface                        │
│                    (Port 8090)                             │
├─────────────────────────────────────────────────────────────┤
│                 Spring AI ChatClient                        │
│                      @Tool System                          │
├─────────────────┬───────────────────────────────────────────┤
│   Direct APIs   │              MCP Servers                 │
│                 │                                          │
│ ┌─────────────┐ │ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ │
│ │   GitHub    │ │ │Weather  │ │  News   │ │Translate│ │Calendar │ │
│ │    Jira     │ │ │ :8092   │ │ :8093   │ │ :8094   │ │ :8095   │ │
│ │   Gmail     │ │ │         │ │         │ │         │ │         │ │
│ │   Slack     │ │ │         │ │         │ │         │ │         │ │
│ └─────────────┘ │ └─────────┘ └─────────┘ └─────────┘ └─────────┘ │
└─────────────────┴───────────────────────────────────────────────┘
```

## 🚦 설치 및 실행

### 1. 사전 요구사항
- Java 21 이상
- Git

### 2. 프로젝트 클론
```bash
git clone https://github.com/VibeMonkeys/mcp-monkeys.git
cd mcp-monkeys
```

### 3. 환경변수 설정
`.env` 파일을 생성하거나 시스템 환경변수에 다음 값들을 설정:

```bash
# 필수 - OpenAI API (AI 채팅 기능)
export OPENAI_API_KEY="your-openai-api-key"

# Direct API 설정 (선택사항)
export GITHUB_TOKEN="your-github-token"
export JIRA_URL="https://your-domain.atlassian.net"
export JIRA_EMAIL="your-email@example.com"
export JIRA_TOKEN="your-jira-token"
export GMAIL_CLIENT_ID="your-gmail-client-id"
export GMAIL_CLIENT_SECRET="your-gmail-client-secret"
export GMAIL_REFRESH_TOKEN="your-gmail-refresh-token"
export SLACK_BOT_TOKEN="xoxb-your-slack-bot-token"

# MCP 서버 API 설정 (선택사항)
export OPENWEATHER_API_KEY="your-openweather-api-key"
export NEWS_API_KEY="your-news-api-key"
```

### 4. 시스템 실행

#### 4-1. MCP 서버들 실행 (백그라운드)
```bash
# 날씨 서버 실행
./gradlew :mcp-weather-server:bootRun &

# 뉴스 서버 실행  
./gradlew :mcp-news-server:bootRun &

# 번역 서버 실행
./gradlew :mcp-translate-server:bootRun &

# 캘린더 서버 실행
./gradlew :mcp-calendar-server:bootRun &
```

#### 4-2. 통합 클라이언트 실행
```bash
./gradlew :mcp-client:bootRun
```

### 5. 실행 확인
```bash
# 시스템 상태 확인
curl http://localhost:8090/api/status

# 사용 가능한 도구 확인
curl http://localhost:8090/api/tools

# API 연동 상태 확인
curl -X POST http://localhost:8090/api/api-status
```

## 🔑 API 키 설정

### 필수 설정

#### OpenAI API
```bash
# OpenAI 계정에서 API 키 발급: https://platform.openai.com/api-keys
export OPENAI_API_KEY="sk-proj-your-api-key"
```

### 선택사항 설정

자세한 API 키 설정 방법은 [API_SETUP.md](./API_SETUP.md) 문서를 참조하세요.

#### GitHub API
```bash
# GitHub → Settings → Developer settings → Personal access tokens
export GITHUB_TOKEN="ghp_your-github-token"
```

#### Jira API  
```bash
export JIRA_URL="https://your-domain.atlassian.net"
export JIRA_EMAIL="your-email@example.com"
export JIRA_TOKEN="your-jira-api-token"
```

#### OpenWeatherMap API
```bash
# https://openweathermap.org/api
export OPENWEATHER_API_KEY="your-openweather-api-key"
```

#### NewsAPI
```bash
# https://newsapi.org/
export NEWS_API_KEY="your-news-api-key"
```

## 📱 사용법

### 1. 웹 인터페이스 사용
브라우저에서 `http://localhost:8090` 접속하여 React 프론트엔드 사용

### 2. API 직접 호출

#### AI 채팅
```bash
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "서울 날씨 알려줘"}'
```

#### 특정 도구 사용 예시
```bash
# GitHub 이슈 조회
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "facebook/react 저장소의 이슈 목록을 보여줘"}'

# 뉴스 검색
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "AI 관련 최신 뉴스를 검색해줘"}'

# 번역 기능
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello World를 한국어로 번역해줘"}'
```

## 🌐 API 엔드포인트

### 메인 클라이언트 (포트 8090)

| 메소드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/api/chat` | AI 채팅 인터페이스 |
| GET | `/api/tools` | 사용 가능한 도구 목록 |
| GET | `/api/status` | 시스템 상태 확인 |
| POST | `/api/api-status` | API 연동 상태 점검 |

### MCP 서버들

| 서버 | 포트 | 상태 확인 |
|------|------|----------|
| Weather | 8092 | `http://localhost:8092/actuator/health` |
| News | 8093 | `http://localhost:8093/actuator/health` |
| Translate | 8094 | `http://localhost:8094/actuator/health` |
| Calendar | 8095 | `http://localhost:8095/actuator/health` |

## 🔧 문제 해결

### 1. 일반적인 문제

#### 서버 시작 실패
```bash
# 포트 충돌 확인
lsof -i :8090
lsof -i :8092-8095

# Gradle 데몬 재시작
./gradlew --stop
./gradlew build
```

#### API 키 오류
```bash
# 환경변수 확인
echo $OPENAI_API_KEY
echo $GITHUB_TOKEN

# 로그 확인
tail -f logs/application.log
```

### 2. MCP 연결 문제

#### MCP 서버 연결 실패
1. MCP 서버들이 모두 실행되었는지 확인
2. 포트 8092-8095가 사용 가능한지 확인
3. 방화벽 설정 확인

#### 도구가 인식되지 않음
1. 서버 로그에서 도구 등록 확인
2. Spring AI 자동설정 로그 확인
3. MCP 클라이언트 연결 상태 확인

### 3. 성능 최적화

#### 메모리 사용량 최적화
```bash
# JVM 힙 크기 조정
export JAVA_OPTS="-Xmx2g -Xms1g"
./gradlew :mcp-client:bootRun
```

#### 동시 실행 최적화
```bash
# 멀티코어 빌드
./gradlew build --parallel --max-workers=4
```

## 📝 추가 참고자료

- [API 설정 가이드](./API_SETUP.md) - 상세한 API 키 설정 방법
- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol 명세](https://spec.modelcontextprotocol.io/)

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 라이센스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**🎯 이제 AI가 당신의 업무를 도와드립니다!**

질문하세요: "GitHub 이슈를 생성하고, 관련 뉴스를 찾고, 슬랙에 공유해줘"