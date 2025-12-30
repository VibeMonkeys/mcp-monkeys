---
name: code-review-checklist
description: 코드 리뷰 체크리스트. PR 리뷰, 코드 품질 확인, 코드 검토 요청 시 사용. 프로젝트 표준에 맞는 리뷰 수행.
allowed-tools: Read, Grep, Glob
---

# 코드 리뷰 체크리스트

이 프로젝트의 코드 리뷰 표준입니다.

## 필수 검토 항목

### 1. 금액/수량 계산

**BigDecimal 사용 필수**
```kotlin
// BAD - 정밀도 손실
val total = items.sumOf { it.price.toInt() }

// GOOD - BigDecimal 연산
val total = items.fold(BigDecimal.ZERO) { acc, item ->
    acc + item.price
}
```

**검토 포인트:**
- 금액 계산에 Int/Double 사용하지 않았는가?
- BigDecimal 연산 시 적절한 RoundingMode 사용했는가?

### 2. 날짜/기간 계산

**Period/Duration 사용**
```kotlin
// BAD - 단순 연도 차이
val years = endDate.year - startDate.year

// GOOD - 정확한 기간 계산
val years = Period.between(startDate, endDate).years
```

**검토 포인트:**
- 윤년, 월말 등 엣지 케이스 처리했는가?
- java.time API 사용했는가? (레거시 Date/Calendar X)

### 3. 입력 검증

**ValidationUtils 사용**
```kotlin
// 필수 - 모든 외부 입력에 검증 적용
val name = ValidationUtils.requireNotBlank(input, "이름")
val email = ValidationUtils.validateEmail(emailInput)
ValidationUtils.requirePositive(amount, "수량")
```

**검토 포인트:**
- MCP 도구의 모든 파라미터에 검증 적용했는가?
- 검증 실패 시 명확한 에러 메시지 제공하는가?

### 4. 데이터베이스 쿼리 최적화

**Count 쿼리 분리**
```kotlin
// BAD - 전체 로드 후 메모리에서 count
val count = repository.findAll().count { it.status == Status.ACTIVE }

// GOOD - DB에서 직접 count
val count = repository.countByStatus(Status.ACTIVE)
```

**검토 포인트:**
- 통계용 쿼리가 전체 데이터를 로드하지 않는가?
- N+1 문제가 발생하지 않는가?
- 적절한 인덱스 사용을 고려했는가?

### 5. Null 안전성

**Kotlin Null Safety 활용**
```kotlin
// BAD
val name = user.name ?: throw Exception("...")

// GOOD - 엘비스 연산자 또는 safe call
val name = user?.name ?: "Unknown"
val result = service.find(id)?.toInfo()
```

**검토 포인트:**
- 불필요한 !! 연산자 사용하지 않았는가?
- Optional 대신 nullable 타입 사용했는가?

### 6. 트랜잭션 관리

**적절한 트랜잭션 범위**
```kotlin
@Service
@Transactional(readOnly = true)  // 기본은 읽기 전용
class MyService {

    @Transactional  // 쓰기 작업에만 적용
    fun createEntity() { ... }
}
```

**검토 포인트:**
- 읽기 전용 작업에 readOnly = true 적용했는가?
- 트랜잭션 범위가 적절한가? (너무 넓거나 좁지 않은가)

### 7. 로깅

**적절한 로그 레벨**
```kotlin
logger.info("MCP Tool 호출: toolName - param=$value")
logger.warn("예상치 못한 상태: $status")
logger.error("처리 실패", exception)
```

**검토 포인트:**
- 민감 정보(비밀번호, 토큰 등) 로깅하지 않았는가?
- 적절한 로그 레벨 사용했는가?

## 아키텍처 검토

### 계층 분리

```
Controller → Service → Repository
     ↓          ↓
McpService → Service → Repository
```

**검토 포인트:**
- McpService가 Repository를 직접 호출하지 않는가?
- 비즈니스 로직이 Service 계층에 있는가?

### DTO 변환

```kotlin
// Entity → DTO 변환은 Extension Function으로
private fun Entity.toInfo() = EntityInfo(...)

// Controller/McpService에서 Entity 직접 반환 금지
fun getEntity(): EntityInfo = service.find().toInfo()
```

## 보안 검토

### SQL Injection 방지
- JPA/JPQL 파라미터 바인딩 사용
- Native Query 사용 시 특히 주의

### 민감 정보
- 로그에 비밀번호, API 키 등 노출 금지
- 응답에 불필요한 내부 정보 포함 금지

## 리뷰 결과 형식

```markdown
## 코드 리뷰 결과

### 필수 수정 (Critical)
- [ ] 항목 1: 설명 및 수정 제안

### 권장 수정 (Recommended)
- [ ] 항목 1: 설명 및 수정 제안

### 개선 제안 (Nice to have)
- [ ] 항목 1: 설명

### 잘한 점
- 항목 1
```
