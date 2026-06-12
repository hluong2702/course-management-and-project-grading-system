# Course Management System - REST API

Hệ thống quản lý khóa học & chấm điểm đồ án — Java Web Service (Spring Boot 3 + JWT + Redis + Cloudinary)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.0 |
| Security | Spring Security 6 + JWT (jjwt 0.11.2) |
| Database | MySQL 8 + Spring Data JPA |
| Cache / Blacklist | Redis (token blacklist via TTL) |
| File Storage | Cloudinary (PDF/Word/any) |
| AOP | Spring AOP (performance logging, grade events) |
| Build | Gradle 8 |
| Tests | JUnit 5 + Mockito + MockMvc (27 tests total) |

---

## Quick Start

### 1. Prerequisites
- Java 21, MySQL 8, Redis, (optional) Cloudinary account

### 2. Database
```bash
mysql -u root -p -e "CREATE DATABASE course_management;"
```

### 3. Configure `application.yml`
```yaml
spring.datasource:
  url: jdbc:mysql://localhost:3306/course_management
  username: your_user
  password: your_password

cloudinary:
  cloud-name: your-cloud-name
  api-key:    your-api-key
  api-secret: your-api-secret
```

### 4. Run
```bash
./gradlew bootRun
```

### 5. Seed data (optional)
```bash
mysql -u root -p course_management < sql/init_data.sql
```

Default accounts created on startup:
| Username | Password | Role |
|---|---|---|
| admin | Admin@123 | ADMIN |
| lecturer1 | Lecturer@123 | LECTURER |

---

## API Endpoints Summary

### Auth  `/api/auth/**` — public
| Method | Path | Description |
|---|---|---|
| POST | /api/auth/login | Login → access + refresh token |
| POST | /api/auth/register | Self-register as STUDENT |
| POST | /api/auth/refresh | Rotate refresh token |
| POST | /api/auth/logout | Blacklist access token in Redis |
| POST | /api/auth/change-password | Change own password |
| POST | /api/auth/forgot-password | Reset password by email |

### Admin  `/api/v1/admin/**` — ROLE_ADMIN only
| Method | Path | Description |
|---|---|---|
| GET/PUT/DELETE | /admin/users | Manage users |
| PATCH | /admin/users/{id}/toggle-status | Enable / disable account |
| POST/GET/PUT/DELETE | /admin/courses | Manage courses |

### Courses  `/api/v1/courses/**` — all authenticated roles
| Method | Path | Description |
|---|---|---|
| GET | /courses | List / search courses |
| GET | /courses/{id} | Course detail |
| GET | /courses/{id}/assignments | Assignments in course |
| GET | /courses/assignments/{id} | Assignment detail |
| GET | /courses/{id}/materials | Lecture materials |
| GET | /courses/{id}/enrollments | Enrolled students |

### Lecturer  `/api/v1/lecturer/**` — ROLE_LECTURER
| Method | Path | Description |
|---|---|---|
| POST | /lecturer/assignments | Create assignment |
| GET | /lecturer/submissions/{assignmentId} | View submissions |
| POST | /lecturer/grades | Grade a submission (0-100) |
| POST | /lecturer/materials/course/{id} | Upload lecture material |

### Student  `/api/v1/student/**` — ROLE_STUDENT
| Method | Path | Description |
|---|---|---|
| POST | /student/enrollments/course/{id} | Enroll in course |
| GET | /student/enrollments | My enrollments |
| POST | /student/submissions | Submit GitHub link |
| POST | /student/submissions/upload | Upload PDF/Word report |
| GET | /student/submissions | My submissions |

---

## Key Features

- **JWT Auth** — access token (15 min) + refresh token (7 days), stored in MySQL; logout blacklists via Redis TTL
- **AOP Logging** — `@Around` measures execution time for all service methods; `@AfterReturning` / `@AfterThrowing` on grading events
- **File Upload** — Cloudinary integration; report uploads restricted to PDF and Word (content-type validated)
- **Late Submission Detection** — auto-sets status to `LATE` when student submits after assignment deadline
- **Soft Delete** — courses are deactivated (`active=false`), not physically deleted
- **Pagination** — all list endpoints return `PageResponse<T>` with `page`, `size`, `totalElements`, `totalPages`
- **Global Error Handling** — `GlobalExceptionHandler` maps all exceptions to `ApiResponse<Void>` with correct HTTP status

---

## Tests  (27 total)

```
Service layer  (22 tests across 4 test classes)
  AuthServiceTest       — 5 tests  (login, register, duplicate checks)
  SubmissionServiceTest — 5 tests  (submit, late, duplicate, grade, grade-pending)
  CourseServiceTest     — 6 tests  (create, duplicate, list, search, notFound, softDelete)
  UserServiceTest       — 6 tests  (get, notFound, update, toggle, filter, deleteNotFound)

Controller layer  (22 tests across 5 test classes)
  AuthControllerTest        — 8 tests
  StudentControllerTest     — 4 tests
  LecturerControllerTest    — 3 tests
  AdminUserControllerTest   — 6 tests
  AdminCourseControllerTest — 6 tests
```

```bash
./gradlew test
# HTML report: build/reports/tests/test/index.html
```

---

## Project Structure

```
src/main/java/com/example/course/
├── aspect/         LoggingAspect.java
├── config/         SecurityConfig, CloudinaryConfig, DataInitializer, UserDetailsServiceConfig
├── controller/     Auth, Course, AdminUser, AdminCourse, Lecturer, Student
├── dto/
│   ├── request/    Login, Register, RefreshToken, ChangePassword, ForgotPassword,
│   │               Course, Assignment, Submission, Grade, UserUpdate
│   └── response/   Api, Auth, User, Course, Enrollment, Assignment, Submission,
│                   LectureMaterial, Page
├── entity/         User, Course, Enrollment, Assignment, Submission, LectureMaterial, RefreshToken
├── enums/          Role, SubmissionStatus
├── exception/      AppException, GlobalExceptionHandler
├── filter/         JwtAuthenticationFilter
├── repository/     (7 repositories)
├── service/        (8 interfaces + 8 implementations)
└── util/           JwtUtil
```
