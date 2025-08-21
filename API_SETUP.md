# API 설정 가이드

이 문서는 MCP Monkeys 프로젝트에서 각 외부 API를 설정하는 방법을 설명합니다.

## 📋 환경변수 설정

다음 환경변수들을 설정하면 바로 API 연동이 가능합니다:

```bash
# GitHub API
export GITHUB_TOKEN="your-github-token"

# Jira API
export JIRA_URL="https://your-domain.atlassian.net"
export JIRA_EMAIL="your-email@company.com"
export JIRA_TOKEN="your-jira-api-token"

# Gmail API
export GMAIL_CLIENT_ID="your-client-id.googleusercontent.com"
export GMAIL_CLIENT_SECRET="your-client-secret"
export GMAIL_REFRESH_TOKEN="your-refresh-token"

# Slack API
export SLACK_BOT_TOKEN="xoxb-your-bot-token"

# OpenAI API (필수)
export OPENAI_API_KEY="your-openai-api-key"

# MCP Server APIs (선택사항)
export WEATHER_API_KEY="your-openweathermap-api-key"
export NEWS_API_KEY="your-newsapi-key"
```

## 🔧 각 API 설정 방법

### 1. GitHub API 설정

1. GitHub > Settings > Developer settings > Personal access tokens
2. "Generate new token" 클릭
3. 필요한 스코프 선택:
   - `repo` (저장소 접근)
   - `read:user` (사용자 정보)
   - `read:org` (조직 정보)
4. 생성된 토큰을 `GITHUB_TOKEN`으로 설정

**테스트 명령:**
```bash
curl -H "Authorization: Bearer $GITHUB_TOKEN" https://api.github.com/user
```

### 2. Jira API 설정

1. Jira > 계정 설정 > 보안 > API 토큰 생성
2. 토큰 생성 후 이메일과 함께 설정
3. Basic 인증 사용 (이메일:토큰을 Base64 인코딩)

**테스트 명령:**
```bash
curl -u "$JIRA_EMAIL:$JIRA_TOKEN" "$JIRA_URL/rest/api/3/myself"
```

### 3. Gmail API 설정

1. Google Cloud Console > API 및 서비스 > 사용자 인증 정보
2. OAuth 2.0 클라이언트 ID 생성
3. OAuth Playground에서 refresh token 생성:
   - https://developers.google.com/oauthplayground/
   - Gmail API v1 스코프 사용
   - Authorization Code로 refresh token 획득

**필요한 스코프:**
- `https://www.googleapis.com/auth/gmail.readonly`
- `https://www.googleapis.com/auth/gmail.send`

### 4. Slack API 설정

1. Slack App 생성: https://api.slack.com/apps
2. OAuth & Permissions에서 Bot Token Scopes 추가:
   - `chat:write` (메시지 전송)
   - `channels:history` (채널 히스토리 읽기)
   - `channels:read` (채널 목록 읽기)
3. 워크스페이스에 앱 설치
4. Bot User OAuth Token 사용

**테스트 명령:**
```bash
curl -H "Authorization: Bearer $SLACK_BOT_TOKEN" https://slack.com/api/auth.test
```

## 🚀 빠른 시작

1. 환경변수 설정 후 애플리케이션 실행:
```bash
./gradlew :mcp-client:bootRun
```

2. API 상태 확인:
```bash
curl -X POST http://localhost:8090/api/api-status
```

3. 사용 가능한 도구 확인:
```bash
curl http://localhost:8090/api/tools
```

4. AI와 대화 시작:
```bash
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "GitHub에서 내 저장소 목록을 가져와줘"}'
```

## 🛠️ 사용 가능한 도구들

### 직접 API 연동 (mcp-client 내장)
#### GitHub
- `getGitHubIssues`: 저장소 이슈 조회
- `createGitHubIssue`: 새 이슈 생성

#### Jira
- `getJiraIssues`: 프로젝트 이슈 조회
- `createJiraIssue`: 새 이슈 생성

#### Gmail
- `getGmailMessages`: 메일 목록 조회
- `sendGmailMessage`: 메일 발송

#### Slack
- `sendSlackMessage`: 메시지 전송
- `getSlackMessages`: 채널 메시지 조회

### MCP 서버를 통한 연동 (포트 8092-8095)
#### Weather Server (8092)
- `getCurrentWeather`: 현재 날씨 조회
- `getWeatherForecast`: 날씨 예보 조회
- `compareWeather`: 여러 도시 날씨 비교

#### News Server (8093)
- `getTopHeadlines`: 최신 뉴스 헤드라인
- `searchNews`: 키워드로 뉴스 검색
- `getNewsBySource`: 특정 출처의 뉴스 조회

#### Translation Server (8094)
- (구현 예정)

#### Calendar Server (8095)
- (구현 예정)

### 시스템
- `checkAllApiStatus`: 모든 API 연결 상태 확인

## 🔍 문제 해결

### "dummy-token" 오류
- 환경변수가 제대로 설정되지 않았습니다
- 애플리케이션 재시작 필요

### "Authentication Failed" 오류
- API 토큰/인증 정보가 올바르지 않습니다
- 토큰 만료 여부 확인

### "Connection Failed" 오류
- 네트워크 연결 문제
- API 엔드포인트 URL 확인

## 📱 프론트엔드 연동

React 프론트엔드는 포트 3004에서 실행됩니다:
```bash
cd mcp-front
npm install
npm run dev
```

브라우저에서 http://localhost:3004 접속하여 UI를 통해 테스트할 수 있습니다.