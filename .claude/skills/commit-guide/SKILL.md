---
name: commit-message-guide
description: 커밋 메시지 작성 가이드. 커밋 메시지 작성, git commit, 변경사항 커밋 시 사용. 한글 커밋 메시지 형식과 prefix 규칙 적용.
---

# 커밋 메시지 가이드

이 프로젝트의 커밋 메시지 규칙입니다.

## 커밋 메시지 형식

```
<type>: <subject>

<body>
```

## Type (접두사)

| Type | 설명 | 예시 |
|------|------|------|
| `feat` | 새로운 기능 추가 | feat: MCP 도서 검색 도구 추가 |
| `fix` | 버그 수정 | fix: BigDecimal 계산 오류 수정 |
| `refactor` | 코드 리팩토링 (기능 변경 없음) | refactor: 서비스 계층 분리 |
| `perf` | 성능 개선 | perf: 통계 쿼리 최적화 |
| `docs` | 문서 수정 | docs: README 업데이트 |
| `test` | 테스트 추가/수정 | test: 도서 대출 테스트 추가 |
| `chore` | 빌드, 설정 변경 | chore: Gradle 의존성 업데이트 |
| `style` | 코드 포맷팅 | style: 코드 정렬 및 공백 정리 |

## Subject (제목) 규칙

### 한글 작성 원칙

```
# GOOD - 명확하고 구체적
feat: MCP 서버 입력 검증 추가
fix: Employee 근속년수 계산 정확도 개선
perf: 통계 쿼리에서 COUNT 쿼리 분리

# BAD - 모호하거나 불명확
feat: 기능 추가
fix: 버그 수정
refactor: 코드 수정
```

### 작성 팁

1. **무엇을** 했는지 명확히 (어떻게는 body에)
2. **50자 이내**로 간결하게
3. **마침표 없이** 끝내기
4. **현재형**으로 작성 (추가, 수정, 개선)

## Body (본문) 작성

복잡한 변경의 경우 본문 추가:

```
feat: MCP 서버 입력 검증 추가

- shared/util/ValidationUtils.kt 추가: 공통 검증 유틸리티
  - requireNotBlank: 빈 문자열 검증
  - validateEmail: 이메일 형식 검증
  - requirePositive: 양수 검증
- 각 MCP 서버 build.gradle.kts에 shared 모듈 의존성 추가
- LibraryMcpService: 검색 키워드, 이메일 검증 추가
- TodoMcpService: 목록명, 이메일, 제목 검증 추가
```

## 실제 예시 (프로젝트 히스토리)

```bash
# 기능 추가
feat: 프론트엔드 새 MCP 서버 연동
feat: Docker 지원 추가
feat: 동적 MCP 도구 목록 조회 구현

# 버그 수정
fix: Product 통계의 재고 금액 계산 정확도 개선
fix: Employee 근속년수 계산 정확도 개선

# 성능 개선
perf: 통계 쿼리 최적화

# 리팩토링
refactor: 미사용 코드 정리
refactor: 단일 책임 원칙에 따른 채팅 서비스 분리
```

## HEREDOC 사용 (멀티라인)

```bash
git commit -m "$(cat <<'EOF'
feat: 기능 설명

- 변경사항 1
- 변경사항 2
- 변경사항 3

EOF
)"
```

## 커밋 전 체크리스트

- [ ] 적절한 type 선택했는가?
- [ ] subject가 명확하고 간결한가?
- [ ] 관련 없는 변경이 섞여있지 않은가?
- [ ] 테스트/빌드 통과했는가?

## 금지 사항

```bash
# 절대 하지 말 것
git commit --amend  # 이미 push된 커밋
git push --force    # main/master 브랜치
git rebase -i       # 공유된 브랜치
```
