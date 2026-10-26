DROP TABLE IF EXISTS class_info;
CREATE TABLE class_info (
 id BIGINT PRIMARY KEY, class_code VARCHAR(30) NOT NULL, class_name VARCHAR(100) NOT NULL,
 college_id BIGINT NOT NULL, major_id BIGINT, enrollment_year SMALLINT, student_count INT NOT NULL,
 created_at TIMESTAMP, updated_at TIMESTAMP
);
CREATE TABLE counselor_class (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, counselor_id BIGINT NOT NULL, class_id BIGINT NOT NULL,
 role_type TINYINT NOT NULL, assigned_at TIMESTAMP
);
CREATE TABLE employment_filing(id BIGINT PRIMARY KEY, class_id BIGINT);
