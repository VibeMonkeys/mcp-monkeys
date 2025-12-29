-- 할일 관리 시스템 초기 데이터 (DML)

-- 태그 데이터
INSERT INTO tags (name, color) VALUES
('업무', '#FF5733'),
('개인', '#33FF57'),
('긴급', '#FF0000'),
('학습', '#3357FF'),
('건강', '#FF33F1'),
('쇼핑', '#FFD700'),
('여행', '#00CED1'),
('프로젝트', '#9932CC');

-- 할일 목록 데이터
INSERT INTO todo_lists (name, description, owner_email) VALUES
('일일 업무', '매일 처리해야 할 업무 목록', 'kim@example.com'),
('프로젝트 A', 'Spring AI 기반 MCP 서버 개발', 'kim@example.com'),
('개인 할일', '개인적인 일들', 'kim@example.com'),
('학습 목표', '이번 분기 학습 계획', 'lee@example.com'),
('집안일', '주간 집안일 목록', 'lee@example.com'),
('여행 계획', '겨울 휴가 여행 준비', 'park@example.com');

-- 할일 데이터
INSERT INTO todos (title, description, todo_list_id, status, priority, due_date, completed_at) VALUES
-- 일일 업무 (id: 1)
('이메일 확인', '아침 이메일 확인 및 답장', 1, 'COMPLETED', 'MEDIUM', CURRENT_DATE, CURRENT_TIMESTAMP),
('주간 회의 준비', '월요일 주간 회의 자료 준비', 1, 'IN_PROGRESS', 'HIGH', CURRENT_DATE + 1, NULL),
('코드 리뷰', 'PR #123 코드 리뷰', 1, 'PENDING', 'MEDIUM', CURRENT_DATE + 2, NULL),
('일일 보고서 작성', '금일 업무 보고서 작성', 1, 'PENDING', 'LOW', CURRENT_DATE, NULL),

-- 프로젝트 A (id: 2)
('API 설계', 'MCP 서버 API 엔드포인트 설계', 2, 'COMPLETED', 'HIGH', CURRENT_DATE - 5, CURRENT_TIMESTAMP),
('엔티티 모델링', '도메인 엔티티 설계 및 구현', 2, 'COMPLETED', 'HIGH', CURRENT_DATE - 3, CURRENT_TIMESTAMP),
('서비스 레이어 구현', '비즈니스 로직 구현', 2, 'IN_PROGRESS', 'HIGH', CURRENT_DATE + 5, NULL),
('테스트 코드 작성', 'Kotest + Fixture Monkey 테스트 작성', 2, 'PENDING', 'MEDIUM', CURRENT_DATE + 7, NULL),
('문서화', 'API 문서 작성', 2, 'PENDING', 'LOW', CURRENT_DATE + 10, NULL),

-- 개인 할일 (id: 3)
('운동하기', '헬스장 가서 운동하기', 3, 'PENDING', 'MEDIUM', CURRENT_DATE, NULL),
('독서', '"클린 코드" 3장 읽기', 3, 'IN_PROGRESS', 'LOW', CURRENT_DATE + 3, NULL),
('생일 선물 구매', '친구 생일 선물 주문', 3, 'PENDING', 'URGENT', CURRENT_DATE - 1, NULL),  -- 연체
('병원 예약', '정기 건강검진 예약', 3, 'PENDING', 'MEDIUM', CURRENT_DATE + 14, NULL),

-- 학습 목표 (id: 4)
('Kotlin 코루틴 학습', '코루틴 기초부터 심화까지 학습', 4, 'IN_PROGRESS', 'HIGH', CURRENT_DATE + 30, NULL),
('Spring AI 문서 정리', 'Spring AI 공식 문서 읽고 정리', 4, 'PENDING', 'MEDIUM', CURRENT_DATE + 15, NULL),
('알고리즘 문제 풀기', '백준 문제 5개 풀기', 4, 'PENDING', 'LOW', CURRENT_DATE + 7, NULL),

-- 집안일 (id: 5)
('청소하기', '주말 대청소', 5, 'PENDING', 'MEDIUM', CURRENT_DATE + 2, NULL),
('장보기', '주간 식료품 구매', 5, 'PENDING', 'HIGH', CURRENT_DATE + 1, NULL),
('빨래', '세탁 및 정리', 5, 'COMPLETED', 'LOW', CURRENT_DATE - 1, CURRENT_TIMESTAMP),

-- 여행 계획 (id: 6)
('항공권 예약', '인천-도쿄 항공권 예약', 6, 'COMPLETED', 'URGENT', CURRENT_DATE - 10, CURRENT_TIMESTAMP),
('숙소 예약', '도쿄 호텔 예약', 6, 'COMPLETED', 'HIGH', CURRENT_DATE - 7, CURRENT_TIMESTAMP),
('여행 일정 작성', '일별 관광 일정 계획', 6, 'IN_PROGRESS', 'MEDIUM', CURRENT_DATE + 5, NULL),
('환전하기', '일본 엔화 환전', 6, 'PENDING', 'MEDIUM', CURRENT_DATE + 20, NULL),
('여권 확인', '여권 유효기간 확인', 6, 'COMPLETED', 'HIGH', CURRENT_DATE - 15, CURRENT_TIMESTAMP);

-- 태그 연결
INSERT INTO todo_tags (todo_id, tag_id) VALUES
-- 업무 태그
(1, 1), (2, 1), (3, 1), (4, 1),
(5, 1), (6, 1), (7, 1), (8, 1), (9, 1),
(5, 8), (6, 8), (7, 8), (8, 8), (9, 8),  -- 프로젝트 태그
-- 개인 태그
(10, 2), (11, 2), (12, 2), (13, 2),
(10, 5),  -- 건강 태그
(11, 4),  -- 학습 태그
(12, 3), (12, 6),  -- 긴급, 쇼핑 태그
-- 학습 태그
(14, 4), (15, 4), (16, 4),
-- 집안일 - 개인 태그
(17, 2), (18, 2), (18, 6), (19, 2),
-- 여행 태그
(20, 7), (21, 7), (22, 7), (23, 7), (24, 7);
