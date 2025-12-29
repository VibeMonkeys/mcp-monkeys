-- 직원 관리 시스템 초기 데이터 (DML)

-- 직급 데이터
INSERT INTO positions (name, level, description, min_salary, max_salary) VALUES
('사원', 1, '신입 및 일반 사원', 35000000, 45000000),
('대리', 2, '3-5년차 직원', 45000000, 55000000),
('과장', 3, '5-8년차 중간관리자', 55000000, 70000000),
('차장', 4, '8-12년차 관리자', 70000000, 85000000),
('부장', 5, '12년차 이상 부서 책임자', 85000000, 110000000),
('이사', 6, '임원급 경영진', 110000000, 150000000),
('상무', 7, '상급 임원', 150000000, 200000000),
('전무', 8, '경영진', 200000000, 250000000),
('사장', 9, '최고 경영자', 300000000, 500000000);

-- 부서 데이터 (매니저 없이 먼저 생성)
INSERT INTO departments (name, code, description) VALUES
('경영지원팀', 'MGMT', '경영 전반 지원 및 총무 업무'),
('인사팀', 'HR', '인사 관리 및 채용 업무'),
('재무팀', 'FIN', '회계 및 재무 관리'),
('개발팀', 'DEV', '소프트웨어 개발'),
('QA팀', 'QA', '품질 보증 및 테스트'),
('마케팅팀', 'MKT', '마케팅 및 홍보'),
('영업팀', 'SALES', '영업 및 고객 관리'),
('디자인팀', 'DESIGN', 'UI/UX 및 그래픽 디자인');

-- 직원 데이터
INSERT INTO employees (employee_number, name, email, phone, department_id, position_id, hire_date, salary, status) VALUES
-- 경영지원팀
('EMP001', '김영수', 'kim.ys@company.com', '010-1234-5678', 1, 5, '2010-03-15', 95000000, 'ACTIVE'),
('EMP002', '이미영', 'lee.my@company.com', '010-2345-6789', 1, 2, '2019-06-01', 52000000, 'ACTIVE'),

-- 인사팀
('EMP003', '박지훈', 'park.jh@company.com', '010-3456-7890', 2, 5, '2012-01-10', 92000000, 'ACTIVE'),
('EMP004', '최수진', 'choi.sj@company.com', '010-4567-8901', 2, 3, '2017-09-20', 62000000, 'ACTIVE'),
('EMP005', '정민수', 'jung.ms@company.com', '010-5678-9012', 2, 1, '2023-02-15', 38000000, 'ACTIVE'),

-- 재무팀
('EMP006', '한지원', 'han.jw@company.com', '010-6789-0123', 3, 4, '2014-07-01', 78000000, 'ACTIVE'),
('EMP007', '강민정', 'kang.mj@company.com', '010-7890-1234', 3, 2, '2020-03-10', 48000000, 'ACTIVE'),

-- 개발팀
('EMP008', '이승호', 'lee.sh@company.com', '010-8901-2345', 4, 5, '2011-05-20', 105000000, 'ACTIVE'),
('EMP009', '김태희', 'kim.th@company.com', '010-9012-3456', 4, 4, '2015-08-15', 82000000, 'ACTIVE'),
('EMP010', '박서준', 'park.sj@company.com', '010-0123-4567', 4, 3, '2018-11-01', 68000000, 'ACTIVE'),
('EMP011', '정유진', 'jung.yj@company.com', '010-1111-2222', 4, 2, '2021-01-20', 55000000, 'ACTIVE'),
('EMP012', '최민호', 'choi.mh@company.com', '010-2222-3333', 4, 1, '2023-07-01', 42000000, 'ACTIVE'),
('EMP013', '한소희', 'han.sh@company.com', '010-3333-4444', 4, 1, '2024-01-15', 38000000, 'ACTIVE'),

-- QA팀
('EMP014', '윤지현', 'yoon.jh@company.com', '010-4444-5555', 5, 4, '2013-04-10', 75000000, 'ACTIVE'),
('EMP015', '서민재', 'seo.mj@company.com', '010-5555-6666', 5, 2, '2020-09-01', 50000000, 'ACTIVE'),
('EMP016', '임수연', 'im.sy@company.com', '010-6666-7777', 5, 1, '2022-06-20', 40000000, 'ACTIVE'),

-- 마케팅팀
('EMP017', '조은서', 'cho.es@company.com', '010-7777-8888', 6, 5, '2012-11-05', 88000000, 'ACTIVE'),
('EMP018', '김하늘', 'kim.hn@company.com', '010-8888-9999', 6, 3, '2016-05-15', 60000000, 'ACTIVE'),
('EMP019', '이준호', 'lee.jh@company.com', '010-9999-0000', 6, 1, '2023-04-01', 36000000, 'ACTIVE'),

-- 영업팀
('EMP020', '박민지', 'park.mj@company.com', '010-1010-2020', 7, 5, '2010-08-20', 98000000, 'ACTIVE'),
('EMP021', '김성훈', 'kim.sh@company.com', '010-2020-3030', 7, 3, '2017-02-10', 65000000, 'ACTIVE'),
('EMP022', '이수현', 'lee.suh@company.com', '010-3030-4040', 7, 2, '2019-10-15', 52000000, 'ACTIVE'),
('EMP023', '최원준', 'choi.wj@company.com', '010-4040-5050', 7, 1, '2024-03-01', 35000000, 'ACTIVE'),

-- 디자인팀
('EMP024', '장미연', 'jang.my@company.com', '010-5050-6060', 8, 4, '2014-12-01', 72000000, 'ACTIVE'),
('EMP025', '신예진', 'shin.yj@company.com', '010-6060-7070', 8, 2, '2020-06-15', 48000000, 'ACTIVE'),

-- 휴직/퇴사 직원
('EMP026', '오세훈', 'oh.sh@company.com', '010-7070-8080', 4, 3, '2016-03-10', 58000000, 'ON_LEAVE'),
('EMP027', '남궁현', 'nam.h@company.com', '010-8080-9090', 7, 2, '2018-05-20', 45000000, 'RESIGNED');

-- 부서 매니저 업데이트
UPDATE departments SET manager_id = 1 WHERE code = 'MGMT';
UPDATE departments SET manager_id = 3 WHERE code = 'HR';
UPDATE departments SET manager_id = 6 WHERE code = 'FIN';
UPDATE departments SET manager_id = 8 WHERE code = 'DEV';
UPDATE departments SET manager_id = 14 WHERE code = 'QA';
UPDATE departments SET manager_id = 17 WHERE code = 'MKT';
UPDATE departments SET manager_id = 20 WHERE code = 'SALES';
UPDATE departments SET manager_id = 24 WHERE code = 'DESIGN';
