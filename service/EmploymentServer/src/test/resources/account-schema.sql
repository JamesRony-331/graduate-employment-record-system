DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS student;
CREATE TABLE student (
 uuid BIGINT AUTO_INCREMENT PRIMARY KEY, student_no VARCHAR(32) UNIQUE NOT NULL, name VARCHAR(64) NOT NULL,
 gender INT, birth_date DATE, id_card VARCHAR(18), phone VARCHAR(32), email VARCHAR(128), college_id BIGINT,
 major_id BIGINT, enrollment_year INT, graduation_year INT, education_level VARCHAR(32),
 student_status INT, address VARCHAR(255), created_at TIMESTAMP, updated_at TIMESTAMP, class_id BIGINT
);
CREATE TABLE sys_user (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL,
 password_algorithm VARCHAR(10) NOT NULL DEFAULT 'BCRYPT', account_type VARCHAR(16) NOT NULL DEFAULT 'STAFF',
 student_uuid BIGINT UNIQUE REFERENCES student(uuid), counselor_id BIGINT UNIQUE, real_name VARCHAR(50),
 status INT NOT NULL DEFAULT 1, created_at TIMESTAMP, updated_at TIMESTAMP,
 CHECK ((account_type='STAFF' AND student_uuid IS NULL) OR (account_type='STUDENT' AND student_uuid IS NOT NULL AND counselor_id IS NULL)),
 CHECK (real_name <> '模拟数据库拒绝')
);

DROP TABLE IF EXISTS counselor;
CREATE TABLE counselor (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, counselor_no VARCHAR(30) UNIQUE NOT NULL,
 name VARCHAR(50) NOT NULL, gender INT, phone VARCHAR(20), email VARCHAR(100), hire_date DATE,
 status INT NOT NULL, created_at TIMESTAMP, updated_at TIMESTAMP
);

CREATE TABLE sys_role (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, role_name VARCHAR(50) NOT NULL, role_code VARCHAR(50) UNIQUE NOT NULL,
 status TINYINT NOT NULL DEFAULT 1, sort_order INT NOT NULL DEFAULT 0, remark VARCHAR(500),
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);
CREATE TABLE sys_user_role (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL, role_id BIGINT NOT NULL,
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), UNIQUE(user_id, role_id)
);
INSERT INTO sys_role(role_name, role_code) VALUES ('学生', 'STUDENT');

CREATE TABLE IF NOT EXISTS major(id BIGINT PRIMARY KEY, college_id BIGINT NOT NULL, major_name VARCHAR(100) NOT NULL);
INSERT INTO major VALUES(1,1,'测试专业');
CREATE TABLE IF NOT EXISTS class_info(id BIGINT PRIMARY KEY, class_code VARCHAR(30), class_name VARCHAR(100), college_id BIGINT, major_id BIGINT, enrollment_year SMALLINT, student_count INT, created_at TIMESTAMP, updated_at TIMESTAMP);
INSERT INTO class_info(id,class_code,class_name,college_id,major_id,student_count) VALUES(999,'TEST','注册测试班',1,1,0);

CREATE TABLE IF NOT EXISTS college(id BIGINT PRIMARY KEY, college_name VARCHAR(100));
INSERT INTO college(id,college_name) VALUES(1,'测试学院');
