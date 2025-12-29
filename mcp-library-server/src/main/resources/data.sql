-- 도서관 초기 데이터 (DML)

-- 저자 데이터
INSERT INTO authors (name, nationality, birth_date, biography) VALUES
('김영하', '한국', '1968-11-11', '대한민국의 소설가. 《살인자의 기억법》, 《나는 나를 파괴할 권리가 있다》 등의 작품으로 유명하다.'),
('무라카미 하루키', '일본', '1949-01-12', '일본의 소설가. 《노르웨이의 숲》, 《1Q84》 등 세계적으로 유명한 작품들을 집필했다.'),
('조지 오웰', '영국', '1903-06-25', '영국의 소설가이자 저널리스트. 《1984》, 《동물농장》 등 디스토피아 문학의 대표 작가.'),
('마틴 파울러', '영국', '1963-12-18', '소프트웨어 개발 분야의 저명한 저자이자 연사. 리팩토링, 도메인 주도 설계 등의 책을 저술했다.'),
('로버트 C. 마틴', '미국', '1952-12-05', '소프트웨어 엔지니어이자 저자. 클린 코드, 클린 아키텍처 등의 저서로 유명하다.'),
('에릭 에반스', '미국', '1965-01-01', '도메인 주도 설계(DDD)의 창시자. 《Domain-Driven Design》의 저자.'),
('한강', '한국', '1970-11-27', '대한민국의 소설가. 《채식주의자》로 맨부커 국제상을 수상했다.'),
('베르나르 베르베르', '프랑스', '1961-09-18', '프랑스의 소설가. 《개미》 시리즈로 유명하다.');

-- 도서 데이터
INSERT INTO books (title, isbn, author_id, publisher, published_date, category, description, total_copies, available_copies, status) VALUES
-- 한국 문학
('살인자의 기억법', '978-89-374-3340-1', 1, '문학동네', '2013-05-20', '소설', '알츠하이머에 걸린 연쇄살인범의 이야기를 그린 심리 스릴러 소설.', 3, 2, 'AVAILABLE'),
('나는 나를 파괴할 권리가 있다', '978-89-374-2345-7', 1, '문학동네', '1996-11-01', '소설', '자살 안내인의 이야기를 통해 현대인의 삶과 죽음을 탐구한 소설.', 2, 2, 'AVAILABLE'),

-- 일본 문학
('노르웨이의 숲', '978-89-329-0622-1', 2, '민음사', '1987-09-04', '소설', '와타나베의 대학 시절과 두 여인과의 사랑을 그린 성장 소설.', 5, 3, 'AVAILABLE'),
('1Q84', '978-89-329-1048-8', 2, '문학동네', '2009-05-29', '소설', '평행 세계 1Q84년을 배경으로 한 두 남녀의 이야기.', 4, 4, 'AVAILABLE'),
('상실의 시대', '978-89-329-0900-0', 2, '민음사', '2010-01-01', '소설', '《노르웨이의 숲》의 한국어 번역본 제목이기도 한 하루키의 대표작.', 3, 1, 'AVAILABLE'),

-- 영미 문학
('1984', '978-89-374-8771-8', 3, '민음사', '1949-06-08', '소설', '전체주의 사회의 공포를 그린 디스토피아 소설의 고전.', 4, 2, 'AVAILABLE'),
('동물농장', '978-89-374-8772-5', 3, '민음사', '1945-08-17', '소설', '스탈린 체제를 풍자한 우화 소설.', 3, 3, 'AVAILABLE'),

-- 프로그래밍/기술 도서
('리팩터링', '978-89-6626-268-3', 4, '한빛미디어', '2020-04-01', '프로그래밍', '기존 코드의 디자인을 개선하는 방법을 설명하는 프로그래밍 고전.', 5, 4, 'AVAILABLE'),
('클린 코드', '978-89-6626-083-2', 5, '인사이트', '2013-12-24', '프로그래밍', '읽기 좋은 코드를 작성하는 방법과 원칙을 설명하는 책.', 6, 5, 'AVAILABLE'),
('클린 아키텍처', '978-89-6626-319-2', 5, '인사이트', '2019-08-20', '프로그래밍', '소프트웨어 구조와 설계의 보편 원칙을 설명하는 책.', 4, 3, 'AVAILABLE'),
('도메인 주도 설계', '978-89-6077-227-4', 6, '위키북스', '2011-07-21', '프로그래밍', '복잡한 소프트웨어 개발을 위한 도메인 중심 설계 방법론.', 3, 2, 'AVAILABLE'),

-- 한국 문학 (추가)
('채식주의자', '978-89-546-2070-1', 7, '창비', '2007-10-30', '소설', '채식을 선언한 여성의 이야기. 맨부커 국제상 수상작.', 4, 4, 'AVAILABLE'),
('소년이 온다', '978-89-546-2939-1', 7, '창비', '2014-05-19', '소설', '광주 민주화 운동을 다룬 소설.', 3, 2, 'AVAILABLE'),

-- 프랑스 문학
('개미', '978-89-329-0812-6', 8, '열린책들', '1991-01-01', '소설', '개미의 세계와 인간 세계를 교차시킨 과학 소설.', 4, 3, 'AVAILABLE'),
('타나토노트', '978-89-329-0943-7', 8, '열린책들', '1994-01-01', '소설', '죽음 이후의 세계를 탐험하는 과학자들의 이야기.', 2, 2, 'AVAILABLE');

-- 대출 데이터 (샘플)
INSERT INTO loans (book_id, borrower_name, borrower_email, loan_date, due_date, return_date, status, created_at) VALUES
-- 대출 중인 도서
(1, '홍길동', 'hong@example.com', CURRENT_DATE - 7, CURRENT_DATE + 7, NULL, 'ACTIVE', CURRENT_TIMESTAMP),
(3, '김철수', 'kim@example.com', CURRENT_DATE - 10, CURRENT_DATE + 4, NULL, 'ACTIVE', CURRENT_TIMESTAMP),
(3, '이영희', 'lee@example.com', CURRENT_DATE - 5, CURRENT_DATE + 9, NULL, 'ACTIVE', CURRENT_TIMESTAMP),
(6, '박민수', 'park@example.com', CURRENT_DATE - 3, CURRENT_DATE + 11, NULL, 'ACTIVE', CURRENT_TIMESTAMP),
(6, '최지우', 'choi@example.com', CURRENT_DATE - 14, CURRENT_DATE, NULL, 'ACTIVE', CURRENT_TIMESTAMP),
(5, '강동원', 'kang@example.com', CURRENT_DATE - 20, CURRENT_DATE - 6, NULL, 'ACTIVE', CURRENT_TIMESTAMP), -- 연체
(5, '신민아', 'shin@example.com', CURRENT_DATE - 18, CURRENT_DATE - 4, NULL, 'ACTIVE', CURRENT_TIMESTAMP), -- 연체
(9, '정우성', 'jung@example.com', CURRENT_DATE - 8, CURRENT_DATE + 6, NULL, 'ACTIVE', CURRENT_TIMESTAMP),
(11, '송혜교', 'song@example.com', CURRENT_DATE - 2, CURRENT_DATE + 12, NULL, 'ACTIVE', CURRENT_TIMESTAMP),

-- 반납 완료된 도서
(2, '유재석', 'yoo@example.com', CURRENT_DATE - 30, CURRENT_DATE - 16, CURRENT_DATE - 18, 'RETURNED', CURRENT_TIMESTAMP),
(4, '강호동', 'kang2@example.com', CURRENT_DATE - 25, CURRENT_DATE - 11, CURRENT_DATE - 12, 'RETURNED', CURRENT_TIMESTAMP),
(7, '이효리', 'hyori@example.com', CURRENT_DATE - 21, CURRENT_DATE - 7, CURRENT_DATE - 8, 'RETURNED', CURRENT_TIMESTAMP);
