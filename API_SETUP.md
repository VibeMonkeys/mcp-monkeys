# 환경 설정 가이드

MCP Monkeys 프로젝트 실행을 위한 환경 설정 방법입니다.

## 환경변수 설정

### 필수 설정 (Gemini API 사용 시)

```bash
# Google Cloud 설정
export GOOGLE_CLOUD_PROJECT="your-project-id"
export GOOGLE_CLOUD_LOCATION="asia-northeast1"
```

### Google Cloud 프로젝트 설정

1. Google Cloud Console 접속: https://console.cloud.google.com
2. 새 프로젝트 생성 또는 기존 프로젝트 선택
3. Vertex AI API 활성화:
   ```bash
   gcloud services enable aiplatform.googleapis.com
   ```
4. 인증 설정:
   ```bash
   gcloud auth application-default login
   ```

## 서버 포트 구성

| 서비스 | 포트 | 설명 |
|--------|------|------|
| MCP Client | 8090 | 통합 클라이언트 |
| Library Server | 8091 | 도서관리 |
| Todo Server | 8096 | 할일관리 |
| Employee Server | 8097 | 직원관리 |
| Product Server | 8098 | 상품관리 |
| React Frontend | 3004 | 웹 UI |

## 빠른 시작

### 1. 전체 MCP 서버 실행

```bash
# 백그라운드로 모든 서버 시작
./gradlew :mcp-library-server:bootRun --args='--server.port=8091' &
./gradlew :mcp-todo-server:bootRun --args='--server.port=8096' &
./gradlew :mcp-employee-server:bootRun --args='--server.port=8097' &
./gradlew :mcp-product-server:bootRun --args='--server.port=8098' &

# 통합 클라이언트 실행
GOOGLE_CLOUD_PROJECT="your-project-id" \
GOOGLE_CLOUD_LOCATION="asia-northeast1" \
./gradlew :mcp-client:bootRun --args='--server.port=8090'
```

### 2. 상태 확인

```bash
# MCP 서버 연결 상태
curl http://localhost:8090/api/health/mcp-servers

# 전체 시스템 상태
curl http://localhost:8090/api/health/comprehensive

# 사용 가능한 도구 목록
curl http://localhost:8090/api/tools
```

### 3. 프론트엔드 실행

```bash
cd mcp-front
npm install
npm run dev
```

브라우저에서 http://localhost:3004 접속

## MCP 서버별 도구

### Library Server (8091)

| 도구 | 설명 |
|------|------|
| searchBooks | 도서 검색 |
| getBookByIsbn | ISBN으로 도서 조회 |
| getAvailableBooks | 대출 가능 도서 |
| borrowBook | 도서 대출 |
| returnBook | 도서 반납 |
| extendLoan | 대출 연장 |
| getOverdueLoans | 연체 목록 |
| getLibraryStats | 통계 |

### Todo Server (8096)

| 도구 | 설명 |
|------|------|
| createTodoList | 목록 생성 |
| getTodoLists | 목록 조회 |
| createTodo | 할일 생성 |
| getMyTodos | 내 할일 |
| searchTodos | 할일 검색 |
| startTodo | 시작 |
| completeTodo | 완료 |
| cancelTodo | 취소 |
| getOverdueTodos | 기한 초과 |
| getTodoStats | 통계 |

### Employee Server (8097)

| 도구 | 설명 |
|------|------|
| searchEmployees | 직원 검색 |
| getEmployeeByNumber | 사번 조회 |
| getEmployeesByDepartment | 부서별 조회 |
| changeDepartment | 부서 이동 |
| changePosition | 직급 변경 |
| updateSalary | 급여 변경 |
| takeLeave | 휴직 |
| returnFromLeave | 복직 |
| resignEmployee | 퇴사 |
| getEmployeeStats | 통계 |

### Product Server (8098)

| 도구 | 설명 |
|------|------|
| searchProducts | 상품 검색 |
| findProductBySku | SKU 조회 |
| findProductsByCategory | 카테고리별 |
| findProductsByBrand | 브랜드별 |
| addStock | 재고 추가 |
| removeStock | 재고 차감 |
| getLowStockProducts | 재고 부족 |
| activateProduct | 활성화 |
| deactivateProduct | 비활성화 |
| getProductStats | 통계 |

## AI 채팅 사용 예시

```bash
# 도서 검색
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "클린코드 책 찾아줘"}'

# 할일 생성
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "내일까지 보고서 작성 할일 추가해줘"}'

# 직원 검색
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "개발팀 직원 목록 보여줘"}'

# 재고 확인
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "재고 부족한 상품 알려줘"}'

# 복합 질의
curl -X POST http://localhost:8090/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "개발팀 직원들 조회하고, IT 도서 중 재고 부족한 것도 알려줘"}'
```

## 문제 해결

### "Connection refused" 오류

- 해당 MCP 서버가 실행 중인지 확인
- 포트 번호가 올바른지 확인

### "Registered tools: 0" 로그

- `ToolCallbackProvider` 빈이 등록되어 있는지 확인
- MCP_TOOL_REGISTRATION_GUIDE.md 참조

### LazyInitializationException 오류

- MCP 서비스 클래스에 `@Transactional(readOnly = true)` 추가
- 엔티티 연관관계 접근 시 트랜잭션 내에서 처리

### H2 Database 오류

- `schema.sql` 및 `data.sql` 파일 확인
- `application.yml`의 `spring.sql.init.mode: always` 설정 확인
