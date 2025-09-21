# MCP Intent Analyzer

Google Gemini 기반 범용 의도분석 마이크로서비스

## 🎯 개요

MCP(Model Context Protocol) 생태계를 위한 지능형 의도분석기입니다. 사용자의 질문을 분석하여 의도, 우선순위, 감정 톤을 파악하고 구조화된 결과를 제공합니다.

## 🚀 빠른 시작

### 환경 변수 설정

```bash
export SERVER_PORT=8097
export GEMINI_PROJECT_ID=your-gcp-project-id
export GEMINI_API_KEY=your-gemini-api-key
export GEMINI_LOCATION=us-central1
export LOG_LEVEL=info
```

### 서버 실행

```bash
# 의존성 설치
go mod tidy

# 서버 시작
go run cmd/server/main.go
```

서버가 시작되면 `:8097` 포트에서 gRPC 서비스가 실행됩니다.

## 📡 API 사용법

### gRPC 서비스

#### AnalyzeIntent
사용자 입력 텍스트의 의도를 분석합니다.

**요청:**
```json
{
  "text": "배포는 어떻게 하나요?",
  "domain": "slack",
  "user_id": "user123",
  "context_messages": ["이전 대화 내용"]
}
```

**응답:**
```json
{
  "intent_type": "question_how",
  "domain_specific_intent": "deployment_inquiry",
  "keywords": [
    {
      "text": "배포",
      "weight": 0.9,
      "category": "technical"
    }
  ],
  "confidence": 0.85,
  "priority": "P3",
  "emotional_tone": "neutral",
  "reasoning": "사용자가 배포 방법에 대해 질문하고 있습니다."
}
```

#### HealthCheck
서비스 상태를 확인합니다.

```bash
# grpcurl을 사용한 테스트 (protobuf 생성 후)
grpcurl -plaintext localhost:8097 intent.IntentAnalyzer/HealthCheck
```

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   gRPC Client   │───▶│  Intent Handler │───▶│ Intent Service  │
│  (MCP Servers)  │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
                                              ┌─────────────────┐
                                              │ Gemini Client   │
                                              │ (Mock/Real)     │
                                              └─────────────────┘
```

## 🔧 개발 모드

### Mock 클라이언트
Gemini API 키가 없는 경우 자동으로 Mock 클라이언트가 사용됩니다:

```bash
# API 키 없이 실행하면 Mock 모드
unset GEMINI_API_KEY
go run cmd/server/main.go
```

Mock 모드에서는 간단한 키워드 기반 의도 분석을 수행합니다.

### 로깅
구조화된 JSON 로깅을 사용합니다:

```json
{
  "level": "info",
  "msg": "Intent analysis completed",
  "intent": "question_how",
  "confidence": 0.85,
  "time": "2024-01-15T10:30:00Z"
}
```

## 🧪 테스트

```bash
# 단위 테스트 실행
go test ./...

# 특정 패키지 테스트
go test ./internal/service -v
```

## 📝 현재 상태 (Phase 1 MVP)

✅ **완료된 기능:**
- [x] Go 프로젝트 구조 설정
- [x] gRPC 서비스 정의 (protobuf)
- [x] 기본 Gemini API 클라이언트 (Mock/Real)
- [x] 간단한 의도분석 로직
- [x] gRPC 서버 구현
- [x] 헬스체크 및 기본 로깅

🚧 **다음 단계 (Phase 2):**
- [ ] protobuf 코드 생성 도구 설정
- [ ] Slack MCP 서버와의 통합
- [ ] 실제 Gemini API 구현
- [ ] Docker 컨테이너화

## 📂 프로젝트 구조

```
mcp-intent-analyzer/
├── cmd/
│   └── server/
│       └── main.go              # 서버 진입점
├── internal/
│   ├── config/
│   │   └── config.go            # 설정 관리
│   ├── gemini/
│   │   └── client.go            # Gemini API 클라이언트
│   ├── handler/
│   │   └── grpc_handler.go      # gRPC 핸들러
│   └── service/
│       └── intent_service.go    # 비즈니스 로직
├── proto/
│   └── intent/
│       └── intent.proto         # gRPC 서비스 정의
├── scripts/
│   └── generate-proto.sh        # protobuf 생성 스크립트
├── go.mod                       # Go 모듈 정의
└── README.md                    # 이 파일
```

## 🔗 관련 링크

- [PRD 문서](../의도분석기-PRD.md)
- [MCP 프로토콜](https://github.com/anthropics/mcp)
- [Google Gemini API](https://cloud.google.com/vertex-ai/docs/generative-ai/model-reference/gemini)

---

*Phase 1 MVP 완료! 🎉*