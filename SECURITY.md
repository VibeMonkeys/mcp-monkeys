# 보안 가이드

## 환경변수 설정 방법

### 1. 개발 환경

1. `.env.example`을 복사하여 `.env` 파일 생성:
   ```bash
   cp .env.example .env
   ```

2. `.env` 파일에 실제 토큰 값 입력:
   ```bash
   # Slack App 설정 (https://api.slack.com/apps)
   SLACK_BOT_TOKEN=xoxb-실제-봇-토큰
   SLACK_APP_TOKEN=xapp-실제-앱-토큰
   SLACK_SIGNING_SECRET=실제-서명-시크릿
   ```

### 2. 프로덕션 환경

#### 방법 1: 시스템 환경변수
```bash
export SLACK_BOT_TOKEN="xoxb-실제-봇-토큰"
export SLACK_APP_TOKEN="xapp-실제-앱-토큰"
export SLACK_SIGNING_SECRET="실제-서명-시크릿"
```

#### 방법 2: Spring Profile 별 설정 파일
```bash
# application-prod.yml 생성 (Git에 커밋하지 않음)
slack:
  bot-token: "실제-봇-토큰"
  app-token: "실제-앱-토큰"
  signing-secret: "실제-서명-시크릿"
```

#### 방법 3: Docker Secrets (Docker 환경)
```bash
docker run -d \
  --env-file .env \
  your-app-image
```

#### 방법 4: Kubernetes Secrets (K8s 환경)
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: slack-credentials
type: Opaque
data:
  slack-bot-token: <base64-encoded-token>
  slack-app-token: <base64-encoded-token>
```

### 3. 클라우드 환경

- **AWS**: Parameter Store, Secrets Manager
- **GCP**: Secret Manager  
- **Azure**: Key Vault
- **Heroku**: Config Vars
- **Railway**: Environment Variables

## 보안 체크리스트

- [ ] `.env` 파일이 `.gitignore`에 포함되어 있는지 확인
- [ ] 실제 토큰이 코드나 커밋에 포함되지 않았는지 확인
- [ ] `application-*.yml` 파일들이 Git에서 제외되는지 확인
- [ ] Slack 토큰의 권한이 최소한으로 설정되어 있는지 확인
- [ ] 프로덕션 환경에서 HTTPS 사용하는지 확인

## Slack 토큰 생성 방법

1. [Slack API 페이지](https://api.slack.com/apps) 접속
2. "Create New App" → "From scratch" 선택
3. App 이름과 워크스페이스 선택
4. OAuth & Permissions에서 Bot Token Scopes 설정:
   - `channels:history`
   - `channels:read`
   - `chat:write`
   - `reactions:write`
   - `reactions:read`
   - `im:history`
   - `mpim:history`
   - `groups:history`
5. Socket Mode 활성화 후 App-Level Token 생성
6. 봇을 채널에 초대: `/invite @봇이름`

## 문제 해결

### 토큰 에러가 발생하는 경우
1. 토큰이 올바른지 확인 (xoxb-, xapp- 접두사)
2. 봇이 해당 채널에 초대되었는지 확인
3. 필요한 권한(Scopes)이 설정되었는지 확인

### Socket Mode 연결 실패하는 경우
1. App-Level Token이 올바른지 확인
2. Socket Mode가 활성화되었는지 확인
3. Event Subscriptions가 Socket Mode에서 활성화되었는지 확인