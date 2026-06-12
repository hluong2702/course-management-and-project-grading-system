-- ============================================================
-- Course Management System - Database Init Script
-- Run AFTER Spring Boot creates tables (ddl-auto: update)
-- ============================================================

-- Clear existing data (for dev/test reset)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE submissions;
TRUNCATE TABLE lecture_materials;
TRUNCATE TABLE assignments;
TRUNCATE TABLE enrollments;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE courses;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- USERS (passwords are BCrypt of the plain-text shown)
-- Admin@123 / Lecturer@123 / Student@123
-- ============================================================
INSERT INTO users (username, password, email, full_name, role, active, created_at, updated_at) VALUES
('admin',     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@system.com',        'System Administrator', 'ADMIN',    1, NOW(), NOW()),
('lecturer1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'lecturer1@university.edu', 'Dr. Nguyen Van A',     'LECTURER', 1, NOW(), NOW()),
('lecturer2', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'lecturer2@university.edu', 'Dr. Tran Thi B',       'LECTURER', 1, NOW(), NOW()),
('student1',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'student1@student.edu',    'Nguyen Van Hoang',     'STUDENT',  1, NOW(), NOW()),
('student2',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'student2@student.edu',    'Tran Thi Mai',         'STUDENT',  1, NOW(), NOW()),
('student3',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'student3@student.edu',    'Le Van Binh',          'STUDENT',  1, NOW(), NOW());

-- ============================================================
-- COURSES
-- ============================================================
INSERT INTO courses (name, description, code, lecturer_id, active, created_at, updated_at) VALUES
('Java Web Service',        'Build RESTful APIs with Spring Boot',   'JWS301',  2, 1, NOW(), NOW()),
('Python for Data Science', 'NumPy, Pandas, Scikit-learn',          'PDS201',  3, 1, NOW(), NOW()),
('Database Systems',        'Relational DB design and optimization', 'DBS101',  2, 1, NOW(), NOW());

-- ============================================================
-- ENROLLMENTS
-- ============================================================
INSERT INTO enrollments (student_id, course_id, enrolled_at) VALUES
(4, 1, NOW()),  -- student1 -> Java Web Service
(4, 2, NOW()),  -- student1 -> Python for Data Science
(5, 1, NOW()),  -- student2 -> Java Web Service
(6, 3, NOW());  -- student3 -> Database Systems

-- ============================================================
-- ASSIGNMENTS
-- ============================================================
INSERT INTO assignments (title, description, course_id, deadline, created_at) VALUES
('Session 01 - REST Basics',          'Implement CRUD for Student entity',         1, DATE_ADD(NOW(), INTERVAL 7  DAY), NOW()),
('Session 07 - JWT Authentication',   'Add JWT login + refresh to your API',       1, DATE_ADD(NOW(), INTERVAL 14 DAY), NOW()),
('Final Project - Full API',          'Complete course management REST API',       1, DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()),
('Pandas DataFrame Analysis',         'Analyse a CSV dataset with Pandas',         2, DATE_ADD(NOW(), INTERVAL 10 DAY), NOW()),
('ER Diagram - University Database',  'Draw ER diagram for university schema',     3, DATE_ADD(NOW(), INTERVAL 5  DAY), NOW());

-- ============================================================
-- SUBMISSIONS (student1 has submitted assignment 1)
-- ============================================================
INSERT INTO submissions (student_id, assignment_id, github_url, status, score, feedback, submitted_at) VALUES
(4, 1, 'https://github.com/student1/jws-session01', 'GRADED', 88, 'Good implementation, minor issues with error handling.', NOW()),
(5, 1, 'https://github.com/student2/jws-session01', 'SUBMITTED', NULL, NULL, NOW());
