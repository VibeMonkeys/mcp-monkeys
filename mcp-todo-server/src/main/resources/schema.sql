-- 할일 관리 시스템 DDL

-- 할일 목록 테이블
CREATE TABLE todo_lists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    owner_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 할일 테이블
CREATE TABLE todos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description VARCHAR(2000),
    todo_list_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    due_date DATE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_todo_list FOREIGN KEY (todo_list_id) REFERENCES todo_lists(id) ON DELETE CASCADE
);

-- 태그 테이블
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(20) DEFAULT '#808080',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 할일-태그 연결 테이블
CREATE TABLE todo_tags (
    todo_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (todo_id, tag_id),
    CONSTRAINT fk_todo_tag_todo FOREIGN KEY (todo_id) REFERENCES todos(id) ON DELETE CASCADE,
    CONSTRAINT fk_todo_tag_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- 인덱스
CREATE INDEX idx_todo_list_owner ON todo_lists(owner_email);
CREATE INDEX idx_todo_status ON todos(status);
CREATE INDEX idx_todo_priority ON todos(priority);
CREATE INDEX idx_todo_due_date ON todos(due_date);
CREATE INDEX idx_todo_list_id ON todos(todo_list_id);
CREATE INDEX idx_tag_name ON tags(name);
