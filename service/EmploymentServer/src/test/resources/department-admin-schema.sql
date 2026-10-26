DROP TABLE IF EXISTS college_admin;
DROP TABLE IF EXISTS college;
CREATE TABLE college (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, college_code VARCHAR(20) NOT NULL UNIQUE,
 college_name VARCHAR(100) NOT NULL, dean_name VARCHAR(50), phone VARCHAR(20), description VARCHAR(500),
 created_at TIMESTAMP, updated_at TIMESTAMP
);
CREATE TABLE college_admin (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL REFERENCES sys_user(id),
 name VARCHAR(50) NOT NULL, college_id BIGINT NOT NULL REFERENCES college(id),
 created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL,
 CHECK (name <> '模拟资料失败')
);
CREATE TABLE IF NOT EXISTS class_info(id BIGINT PRIMARY KEY, college_id BIGINT);
