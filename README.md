# MCP Monkeys - Multi-Service MCP Architecture

진짜 MCP (Model Context Protocol) 아키텍처로 구현된 다중 외부 서비스 연동 시스템

## 🏗️ 아키텍처

```
React Frontend (3000)
    ↓ HTTP REST API
MCP Client (8090) - 통합 관리자 + OpenAI ChatClient
    ↓ ↓ ↓ ↓ (HTTP 통신)
GitHub MCP  Jira MCP   Gmail MCP   Slack MCP  
Server      Server     Server      Server
(8092)      (8093)     (8094)      (8095)
```

## 📁 프로젝트 구조

```
mcp-monkeys/
├── shared/                    # 공통 DTO (GitHub, Jira, Gmail, Slack)
├── mcp-github-server/         # GitHub MCP 서버 
├── mcp-jira-server/          # Jira MCP 서버
├── mcp-gmail-server/         # Gmail MCP 서버  
├── mcp-slack-server/         # Slack MCP 서버
└── mcp-client/               # 통합 MCP 클라이언트
```

## 🔧 각 MCP 서버 기능

### GitHub MCP Server (8092)
- `@Tool getIssues()` - 이슈 목록 조회
- `@Tool createIssue()` - 이슈 생성
- `@Tool getPullRequests()` - PR 목록 조회
- `@Tool getRepository()` - 저장소 정보 조회

### Jira MCP Server (8093)
- `@Tool getIssues()` - 프로젝트 이슈 조회
- `@Tool createIssue()` - 이슈 생성
- `@Tool getProject()` - 프로젝트 정보
- `@Tool getActiveSprints()` - 활성 스프린트 조회

### Gmail MCP Server (8094)
- `@Tool getMessages()` - 메일 목록 조회
- `@Tool sendMessage()` - 메일 발송
- `@Tool getLabels()` - 라벨 목록 조회

### Slack MCP Server (8095)
- `@Tool sendMessage()` - 채널 메시지 전송
- `@Tool getMessages()` - 채널 메시지 조회
- `@Tool getChannels()` - 채널 목록 조회
- `@Tool getUsers()` - 사용자 목록 조회

### 통합 MCP Client (8090)
- 모든 MCP 서버와 연결
- OpenAI ChatClient + Spring AI 통합
- React 친화적 REST API 제공

## 🚀 사용 방법

### 1. 환경 변수 설정

```bash
export OPENAI_API_KEY=your-openai-api-key
export GITHUB_TOKEN=your-github-token
export JIRA_URL=https://your-domain.atlassian.net
export JIRA_EMAIL=your@email.com
export JIRA_TOKEN=your-jira-token
export GMAIL_CLIENT_ID=your-gmail-client-id
export GMAIL_CLIENT_SECRET=your-gmail-client-secret
export SLACK_BOT_TOKEN=your-slack-bot-token
```

### 2. 서버 실행

```bash
# 모든 MCP 서버 병렬 실행
./gradlew :mcp-github-server:bootRun --args='--server.port=8092' &
./gradlew :mcp-jira-server:bootRun --args='--server.port=8093' &  
./gradlew :mcp-gmail-server:bootRun --args='--server.port=8094' &
./gradlew :mcp-slack-server:bootRun --args='--server.port=8095' &

# 통합 클라이언트 실행
./gradlew :mcp-client:bootRun --args='--server.port=8090'
```

### 3. React에서 API 사용

```javascript
// GitHub 이슈 조회
const response = await fetch('/api/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    message: 'facebook/react 저장소의 최근 이슈 10개 보여줘'
  })
});

// 여러 시스템 연계 작업
const response = await fetch('/api/chat', {
  method: 'POST', 
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    message: 'GitHub에서 버그 이슈를 찾아서 Jira에 동기화하고 Slack으로 알림 보내줘'
  })
});
```

## 📡 API 엔드포인트

### 통합 MCP Client (8090)
- `POST /api/chat` - AI 채팅 (모든 MCP 도구 사용 가능)
- `GET /api/tools` - 사용 가능한 도구 목록
- `GET /api/status` - 클라이언트 상태
- `POST /api/mcp-status` - 모든 MCP 서버 상태 확인

### 각 MCP 서버 공통
- `GET /mcp/{service}/tools` - 해당 서비스 도구 목록
- `GET /mcp/{service}/health` - 서비스 상태
- `GET /actuator/health` - Spring Boot 헬스체크

## 🛠️ 기술 스택

- **Backend**: Spring Boot 3.5.4, Kotlin 1.9.25
- **AI Integration**: Spring AI 1.0.1, OpenAI GPT
- **MCP Protocol**: Spring AI MCP Server/Client
- **HTTP Client**: OkHttp 4.12.0  
- **Database**: H2 (in-memory)
- **Build Tool**: Gradle 8.x

## 🎯 주요 특징

1. **진짜 MCP 아키텍처**: 각 외부 API를 독립적인 MCP 서버로 분리
2. **Spring AI 완전 활용**: `@Tool`, 구조화된 출력, BeanOutputConverter
3. **React 친화적**: CORS 설정, REST API, JSON 응답
4. **확장 가능**: 새로운 외부 API 서버 추가 용이
5. **타입 안전**: 공통 DTO 모듈로 타입 일관성 보장

## 🔧 개발 노트

- Gmail, Slack API는 현재 더미 데이터 반환 (실제 API 연동 필요)
- GitHub, Jira API는 실제 연동 준비 완료 (토큰 설정 필요)
- 모든 MCP 서버는 독립적으로 실행 및 테스트 가능
- OpenAI API 키 없이도 구조 테스트 가능 (401 에러 정상)
