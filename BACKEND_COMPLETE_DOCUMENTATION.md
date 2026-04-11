# CollegeNet Backend - Complete Documentation

**Project Type:** Spring Boot 3.5.9 + PostgreSQL  
**Java Version:** 17  
**Build Tool:** Maven  
**Date Generated:** February 18, 2026

---

## 📑 Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Database Schema](#database-schema)
4. [Entity Classes](#entity-classes)
5. [Data Transfer Objects (DTOs)](#data-transfer-objects-dtos)
6. [REST API Endpoints](#rest-api-endpoints)
7. [Controllers](#controllers)
8. [Services](#services)
9. [Repositories](#repositories)
10. [Configuration & Security](#configuration--security)
11. [Exception Handling](#exception-handling)
12. [Authentication Flow](#authentication-flow)
13. [Database Connection Details](#database-connection-details)

---

## Project Overview

CollegeNet is a comprehensive College Management System built with Spring Boot and PostgreSQL. It manages:
- **User Management** (Students, Colleges, Admins)
- **Course Management**
- **Student Enrollments**
- **Fee & Payment Management**
- **Marks & Results Management**
- **College Approval System**
- **Admin Dashboard & Reporting**

---

## Technology Stack

```
Backend Framework:      Spring Boot 3.5.9
Language:              Java 17
Database:              PostgreSQL
ORM:                   Spring Data JPA / Hibernate
Security:              Spring Security
Validation:            Jakarta Validation
Build Tool:            Maven
API Type:              RESTful JSON APIs
CORS:                  Enabled (localhost:3000)
```

### Key Dependencies (from pom.xml):
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-web`
- `postgresql` (JDBC Driver)
- `lombok` (Annotations)

---

## Database Schema

### Database Configuration
```yaml
# PostgreSQL Connection
Host:     localhost
Port:     5432
Database: collegenet_db
Username: postgres
Password: chandan

# Hibernate Configuration
DDL Mode: update
SQL Formatting: enabled
Dialect: PostgreSQL
Batch Operations: enabled (20 batch size)
```

### Tables Overview

#### 1. **users** (User Authentication)
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),  -- BCrypt encrypted
    role VARCHAR(50) NOT NULL,  -- STUDENT, COLLEGE, ADMIN
    phone VARCHAR(20)
);
```

#### 2. **colleges** (College Registration)
```sql
CREATE TABLE colleges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    college_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    approved BOOLEAN DEFAULT FALSE,
    user_id BIGINT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 3. **courses** (Course Catalog)
```sql
CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE PRECISION,
    job_guaranteed BOOLEAN,
    duration_in_weeks INTEGER
);
```

#### 4. **students** (Student Profile)
```sql
CREATE TABLE students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    college_id BIGINT,
    enrollment_number VARCHAR(255) UNIQUE NOT NULL,
    course VARCHAR(255),
    semester INTEGER,
    attendance_percentage DOUBLE PRECISION,
    fees_paid BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (college_id) REFERENCES colleges(id)
);
```

#### 5. **student_courses** (Enrollment Relationship)
```sql
CREATE TABLE student_courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_date DATE NOT NULL,
    status VARCHAR(50),  -- ACTIVE, COMPLETED, DROPPED
    UNIQUE (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);
```

#### 6. **semesters** (Academic Semesters)
```sql
CREATE TABLE semesters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    semester_number INTEGER,  -- 1,2,3,4,5,6,7,8
    academic_year VARCHAR(20),  -- 2024-2025
    student_id BIGINT,
    FOREIGN KEY (student_id) REFERENCES students(id)
);
```

#### 7. **exams** (Exam Details)
```sql
CREATE TABLE exams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_type VARCHAR(50),  -- INTERNAL, MID, SEMESTER
    subject VARCHAR(255),
    max_marks INTEGER,
    semester_id BIGINT,
    FOREIGN KEY (semester_id) REFERENCES semesters(id)
);
```

#### 8. **marks** (Student Marks)
```sql
CREATE TABLE marks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT,
    semester INTEGER,
    subject VARCHAR(255),
    exam_type VARCHAR(50),  -- INTERNAL, MID, SEMESTER
    max_marks INTEGER,
    obtained_marks INTEGER,
    pass BOOLEAN
);
```

#### 9. **fees** (Student Fees)
```sql
CREATE TABLE fees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    total_fee DOUBLE PRECISION NOT NULL,
    academic_year VARCHAR(20),
    FOREIGN KEY (student_id) REFERENCES students(id)
);
```

#### 10. **payments** (Payment Records)
```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    payment_date DATE NOT NULL,
    payment_mode VARCHAR(50),  -- UPI, CASH, CARD
    FOREIGN KEY (student_id) REFERENCES students(id)
);
```

#### 11. **results** (Academic Results)
```sql
CREATE TABLE results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    status VARCHAR(50),
    student_id BIGINT NOT NULL,
    semester INTEGER,
    sgpa DOUBLE PRECISION,  -- Semester GPA
    cgpa DOUBLE PRECISION,  -- Cumulative GPA
    passed BOOLEAN,
    FOREIGN KEY (student_id) REFERENCES students(id)
);
```

---

## Entity Classes

### 1. **User Entity**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;  // BCrypt encrypted

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;  // STUDENT, COLLEGE, ADMIN

    private String phone;
}

public enum Role {
    STUDENT,
    COLLEGE,
    ADMIN
}
```

### 2. **Student Entity**
```java
@Entity
@Table(name = "students")
@Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;  // Login account

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = true)
    private College college;

    @Column(nullable = false, unique = true)
    private String enrollmentNumber;

    private String course;
    private Integer semester;
    private Double attendancePercentage;
    private Boolean feesPaid;
}
```

### 3. **College Entity**
```java
@Entity
@Table(name = "colleges")
public class College {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String collegeName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String address;
    private String city;
    private String state;
    private boolean approved = false;  // Admin approval

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;  // College login account
}
```

### 4. **Course Entity**
```java
@Entity
@Table(name = "courses")
@Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;
    private Double price;
    private Boolean jobGuaranteed;
    private Integer durationInWeeks;
}
```

### 5. **StudentCourse Entity** (Many-to-Many)
```java
@Entity
@Table(name = "student_courses", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
@Builder
public class StudentCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;  // ACTIVE, COMPLETED, DROPPED

    public enum EnrollmentStatus {
        ACTIVE,
        COMPLETED,
        DROPPED
    }
}
```

### 6. **Semester Entity**
```java
@Entity
@Builder
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer semesterNumber;  // 1,2,3,4,5,6,7,8
    private String academicYear;     // 2024-2025

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
}
```

### 7. **Exam Entity**
```java
@Entity
@Builder
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String examType;  // INTERNAL / MID / SEMESTER
    private String subject;
    private Integer maxMarks;

    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;
}
```

### 8. **Marks Entity**
```java
@Entity
@Data
@Builder
public class Marks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Integer semester;
    private String subject;
    private String examType;      // INTERNAL, MID, SEMESTER
    private Integer maxMarks;
    private Integer obtainedMarks;
    private Boolean pass;
}
```

### 9. **Fee Entity**
```java
@Entity
@Table(name = "fees")
@Builder
public class Fee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private Double totalFee;

    private String academicYear;
}
```

### 10. **Payment Entity**
```java
@Entity
@Table(name = "payments")
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;  // UPI, CASH, CARD

    public enum PaymentMode {
        UPI,
        CASH,
        CARD
    }
}
```

### 11. **Result Entity**
```java
@Entity
@Table(name = "results")
@Builder
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private Integer semester;
    private Double sgpa;   // Semester GPA
    private Double cgpa;   // Cumulative GPA
    private Boolean passed;
}
```

---

## Data Transfer Objects (DTOs)

### Authentication DTOs

#### **LoginRequest**
```java
@Data
public class LoginRequest {
    @NotBlank
    private String usernameOrEmail;
    
    @NotBlank
    private String password;
}
```

#### **RegisterRequest**
```java
@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String password;
}
```

#### **AuthResponse**
```java
@Data
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private Long studentId;
    private String username;
    private String message;
}
```

### Student DTOs

#### **StudentResponseDTO**
```java
@Data
@Builder
public class StudentResponseDTO {
    private Long id;
    private String enrollmentNumber;
    private String course;
    private Integer semester;
    private Double attendancePercentage;
    private Boolean feesPaid;
}
```

#### **StudentRequestDTO**
```java
@Data
public class StudentRequestDTO {
    private Long userId;
    private Long collegeId;
    private String enrollmentNumber;
    private String course;
    private Integer semester;
}
```

#### **StudentUpdateDTO**
```java
@Data
@Builder
public class StudentUpdateDTO {
    private String enrollmentNumber;
    private String course;
    private Integer semester;
    private Double attendancePercentage;
    private Boolean feesPaid;
}
```

#### **AdminStudentDTO**
```java
@Data
@Builder
public class AdminStudentDTO {
    private Long id;
    private String enrollmentNumber;
    private String studentName;
    private String collegeName;
    private Long collegeId;
    private String course;
    private Integer semester;
    private Double attendancePercentage;
    private Boolean feesPaid;
    private String email;
    private String phone;
}
```

#### **StudentDashboardResponse**
```java
@Data
public class StudentDashboardResponse {
    private Long studentId;
    private String studentName;
    private String collegeName;
    private String courseName;
    private String currentSemester;
    private Double cgpa;
    private String resultStatus;
    private Double totalFee;
    private Double paidFee;
    private Double dueFee;
}
```

### Course DTOs

#### **CourseDTO**
```java
@Data
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private Boolean jobGuaranteed;
    private Integer durationInWeeks;
}
```

### Enrollment DTOs

#### **EnrollmentRequest**
```java
@Data
@Builder
public class EnrollmentRequest {
    private Long studentId;
    private Long courseId;
}
```

#### **EnrollmentResponse**
```java
@Data
@Builder
public class EnrollmentResponse {
    private Long enrollmentId;
    private Long studentId;
    private String enrollmentNumber;
    private Long courseId;
    private String courseTitle;
    private LocalDate enrollmentDate;
    private StudentCourse.EnrollmentStatus status;
}
```

#### **DropCourseRequest**
```java
@Data
@Builder
public class DropCourseRequest {
    private Long studentId;
    private Long courseId;
}
```

#### **EnrollmentManagementDTO**
```java
@Data
@Builder
public class EnrollmentManagementDTO {
    private Long enrollmentId;
    private Long studentId;
    private String enrollmentNumber;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private String status;          // ACTIVE, COMPLETED, DROPPED
    private String enrollmentDate;
}
```

### Fee & Payment DTOs

#### **FeeDTO**
```java
@Data
public class FeeDTO {
    private Long id;
    private Long studentId;
    private Double amount;
    private String feeType;  // COLLEGE / HOSTEL / COURSE
    private Boolean paid;
}
```

#### **PaymentRequest**
```java
@Data
@Builder
public class PaymentRequest {
    private Long studentId;
    private Double amount;
    private Payment.PaymentMode paymentMode;  // UPI, CASH, CARD
}
```

#### **AssignFeeRequest**
```java
@Data
@Builder
public class AssignFeeRequest {
    private Long studentId;
    private Double totalFee;
    private String academicYear;
}
```

#### **FeeStatusResponse**
```java
@Data
@Builder
public class FeeStatusResponse {
    private Long studentId;
    private Double totalFee;
    private Double paidFee;
    private Double dueFee;
}
```

### Marks & Results DTOs

#### **MarksDTO**
```java
@Data
public class MarksDTO {
    private Long id;
    private Long studentId;
    private Integer semester;
    private String subject;
    private String examType;       // INTERNAL / MID / SEMESTER
    private Integer maxMarks;
    private Integer obtainedMarks;
    private Boolean pass;
}
```

#### **ResultDTO**
```java
@Data
public class ResultDTO {
    private Long id;
    private Long studentId;
    private Integer semester;
    private Double sgpa;
    private Double cgpa;
    private Boolean passed;
}
```

### College DTOs

#### **CollegeApprovalDTO**
```java
@Data
@Builder
public class CollegeApprovalDTO {
    private Long collegeId;
    private String collegeName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private boolean approved;
}
```

### Admin DTOs

#### **AdminReportResponse**
```java
@Data
@Builder
public class AdminReportResponse {
    private Long totalColleges;
    private Long totalStudents;
    private Long totalCourses;
    private Long totalUsers;
    private Long totalPayments;
}
```

### Error DTO

#### **ErrorResponse**
```java
@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
```

---

## REST API Endpoints

### **Authentication Endpoints** (`/api/auth`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/api/auth/register` | RegisterRequest | AuthResponse | Register new user (student) |
| POST | `/api/auth/login` | LoginRequest | AuthResponse | Login user |

**Example:**
```json
// Register
POST /api/auth/register
{
  "username": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}

Response: {
  "userId": 1,
  "studentId": 1,
  "username": "john@example.com",
  "message": "User registered successfully"
}

// Login
POST /api/auth/login
{
  "usernameOrEmail": "john@example.com",
  "password": "password123"
}
```

---

### **Student Endpoints** (`/api/students`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/api/students` | StudentRequestDTO | StudentResponseDTO | Create student |
| GET | `/api/students/{id}` | - | Student | Get student by ID |
| GET | `/api/students` | - | List<Student> | Get all students |
| GET | `/api/students/enrollment/{enrollmentNumber}` | - | Student | Get student by enrollment number |
| GET | `/api/students/{studentId}/dashboard` | - | StudentDashboardResponse | Get student dashboard |

---

### **Course Endpoints** (`/api/courses`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/api/courses` | - | List<CourseDTO> | Get all courses |
| GET | `/api/courses/{id}` | - | CourseDTO | Get course by ID |
| POST | `/api/courses` | Course | Course | Create new course |
| PUT | `/api/courses/{id}` | Course | Course | Update course |
| DELETE | `/api/courses/{id}` | - | Message | Delete course |
| GET | `/api/courses/filter/job-guarantee?guaranteed=true` | - | List<CourseDTO> | Filter by job guarantee |
| GET | `/api/courses/sort/price?ascending=true` | - | List<CourseDTO> | Sort by price |
| GET | `/api/courses/filter/duration?minWeeks=4&maxWeeks=12` | - | List<CourseDTO> | Filter by duration range |
| GET | `/api/courses/search?keyword=java` | - | List<CourseDTO> | Search courses |

---

### **Enrollment Endpoints** (`/api/enrollments`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/api/enrollments/enroll` | EnrollmentRequest | EnrollmentResponse | Enroll student to course |
| GET | `/api/enrollments/student/{studentId}/courses` | - | List<CourseDTO> | Get student's courses |
| GET | `/api/enrollments/course/{courseId}/students` | - | List<StudentResponseDTO> | Get course students |
| PUT | `/api/enrollments/drop` | DropCourseRequest | Message | Drop course |
| GET | `/api/enrollments/status/{studentId}/{courseId}` | - | EnrollmentStatus | Get enrollment status |
| GET | `/api/enrollments/course/{courseId}/count` | - | Count | Get enrolled student count |

---

### **Fee Endpoints** (`/api/fees`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/api/fees` | - | List<Fee> | Get all fees |
| POST | `/api/fees/assign` | AssignFeeRequest | Fee | Assign fee to student |
| POST | `/api/fees/pay` | PaymentRequest | Payment | Record payment |
| GET | `/api/fees/status/{studentId}` | - | FeeStatusResponse | Get fee status |
| GET | `/api/fees/student/{studentId}` | - | Fee | Get student fee details |
| POST | `/api/fees` | Fee | Fee | Create fee |

---

### **Marks Endpoints** (`/api/marks`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/api/marks` | - | List<Marks> | Get all marks |
| POST | `/api/marks` | Marks | Marks | Add marks for student |
| GET | `/api/marks/{studentId}/{semester}` | - | List<Marks> | Get student marks by semester |

---

### **Results Endpoints** (`/api/results`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/api/results` | - | List<Result> | Get all results |
| POST | `/api/results` | Result | Result | Upload result |
| GET | `/api/results/student/{studentId}` | - | List<Result> | Get student results |

---

### **Semester Endpoints** (`/api/semesters`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/api/semesters` | - | List<Semester> | Get all semesters |
| POST | `/api/semesters` | Semester | Semester | Create semester |
| GET | `/api/semesters/student/{studentId}` | - | List<Semester> | Get student semesters |

---

### **Exam Endpoints** (`/api/exams`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/api/exams` | - | List<Exam> | Get all exams |
| POST | `/api/exams` | Exam | Exam | Create exam |
| GET | `/api/exams/semester/{semesterId}` | - | List<Exam> | Get exams by semester |

---

### **College Endpoints** (`/api/colleges`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/api/colleges` | College | College | Register college |
| GET | `/api/colleges` | - | List<College> | Get all colleges |
| GET | `/api/colleges/{id}` | - | College | Get college by ID |
| PUT | `/api/colleges/{id}/approve` | - | College | Approve college |

---

### **Admin Endpoints** (`/api/admin`)

#### Dashboard & Reports
| Method | Endpoint | Response | Description |
|--------|----------|----------|-------------|
| GET | `/api/admin/colleges` | List<College> | Get all colleges |
| GET | `/api/admin/users` | List<User> | Get all users |
| GET | `/api/admin/courses` | List<Course> | Get all courses |
| GET | `/api/admin/reports` | AdminReportResponse | Get aggregated reports |

#### Student Management
| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/api/admin/students` | - | List<AdminStudentDTO> | Get all students |
| GET | `/api/admin/students/{studentId}` | - | AdminStudentDTO | Get student details |
| PUT | `/api/admin/students/{studentId}` | StudentUpdateDTO | AdminStudentDTO | Update student |
| DELETE | `/api/admin/students/{studentId}` | - | Message | Delete student |

#### College Approval Management
| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/api/admin/colleges/approval/all` | - | List<CollegeApprovalDTO> | Get all colleges for approval |
| GET | `/api/admin/colleges/approval/{collegeId}` | - | CollegeApprovalDTO | Get college approval details |
| PUT | `/api/admin/colleges/{collegeId}/approve` | - | CollegeApprovalDTO | Approve college |
| PUT | `/api/admin/colleges/{collegeId}/reject` | - | CollegeApprovalDTO | Reject college |

---

## Controllers

### **1. AuthController** (`/api/auth`)
- Handles user registration and login
- Returns `AuthResponse` with userId, studentId, and message
- Automatically creates Student record for STUDENT role

**File Location:** `com.collegenet.collegenet.controller.AuthController`

---

### **2. StudentController** (`/api/students`)
- CRUD operations for students
- Dashboard endpoint for student-specific data
- Enrollment number lookup

**File Location:** `com.collegenet.collegenet.controller.StudentController`

---

### **3. CourseController** (`/api/courses`)
- Full CRUD for courses
- Advanced filtering: by job guarantee, price, duration
- Course search functionality
- Course listing with sorting

**File Location:** `com.collegenet.collegenet.controller.CourseController`

---

### **4. EnrollmentController** (`/api/enrollments`)
- Student course enrollment management
- Course dropping functionality
- Enrollment status tracking
- Student-course relationship management

**File Location:** `com.collegenet.collegenet.controller.EnrollmentController`

---

### **5. FeeController** (`/api/fees`)
- Fee assignment to students
- Payment recording
- Fee status tracking
- Payment history

**File Location:** `com.collegenet.collegenet.controller.FeeController`

---

### **6. MarksController** (`/api/marks`)
- Add/update student marks
- Retrieve marks by student and semester
- Exam type tracking (INTERNAL, MID, SEMESTER)

**File Location:** `com.collegenet.collegenet.controller.MarksController`

---

### **7. ResultController** (`/api/results`)
- Upload/save academic results
- SGPA/CGPA management
- Student result retrieval

**File Location:** `com.collegenet.collegenet.controller.ResultController`

---

### **8. SemesterController** (`/api/semesters`)
- Semester creation and management
- Link semesters to students

**File Location:** `com.collegenet.collegenet.controller.SemesterController`

---

### **9. ExamController** (`/api/exams`)
- Exam creation
- Retrieve exams by semester

**File Location:** `com.collegenet.collegenet.controller.ExamController`

---

### **10. CollegeController** (`/api/colleges`)
- College registration
- College approval
- College listing

**File Location:** `com.collegenet.collegenet.controller.CollegeController`

---

### **11. AdminController** (`/api/admin`)
- Dashboard & reporting
- Student management
- College approval workflow
- User and course management

**File Location:** `com.collegenet.collegenet.controller.AdminController`

---

## Services

### **1. AuthService**
**Location:** `com.collegenet.collegenet.service.AuthService`

**Methods:**
- `AuthResponse register(RegisterRequest request)` - Register new user
- `AuthResponse login(LoginRequest request)` - Authenticate user
- `String generateEnrollmentNumber()` - Generate unique enrollment number

**Functionality:**
- User registration with role assignment
- Password encryption using BCrypt
- Auto-creates Student record for STUDENT role
- Email validation and duplicate check

---

### **2. StudentService**
**Location:** `com.collegenet.collegenet.service.StudentService`

**Methods:**
- `StudentResponseDTO createStudent(StudentRequestDTO dto)` - Create student
- `Student getStudentById(Long id)` - Fetch student by ID
- `List<Student> getAllStudents()` - Fetch all students
- `Student getByEnrollmentNumber(String enrollmentNumber)` - Fetch by enrollment number
- `StudentDashboardResponse getStudentDashboard(Long studentId)` - Get dashboard data

**Functionality:**
- Student profile management
- Enrollment number generation
- Fee and payment data aggregation
- Student dashboard compilation

---

### **3. CourseService**
**Location:** `com.collegenet.collegenet.service.CourseService`

**Methods:**
- `Course createCourse(Course course)` - Create course
- `Course updateCourse(Long id, Course courseDetails)` - Update course
- `void deleteCourse(Long id)` - Delete course
- `CourseDTO getCourseDTOById(Long id)` - Get course by ID
- `List<CourseDTO> getAllCoursesDTO()` - Get all courses
- `List<CourseDTO> getCoursesByJobGuarantee(Boolean guaranteed)` - Filter by job guarantee
- `List<CourseDTO> getCoursesSortedByPrice(boolean ascending)` - Sort by price
- `List<CourseDTO> getCoursesByDurationRange(Integer minWeeks, Integer maxWeeks)` - Filter by duration
- `List<CourseDTO> searchCoursesByTitle(String keyword)` - Search courses

---

### **4. EnrollmentService**
**Location:** `com.collegenet.collegenet.service.EnrollmentService`

**Methods:**
- `StudentCourse enrollStudentToCourse(Long studentId, Long courseId)` - Enroll student
- `void dropCourse(Long studentId, Long courseId)` - Drop course
- `List<CourseDTO> getStudentCourses(Long studentId)` - Get student's courses
- `List<StudentResponseDTO> getCourseStudents(Long courseId)` - Get course students
- `StudentCourse.EnrollmentStatus getEnrollmentStatus(Long studentId, Long courseId)` - Get enrollment status
- `long getEnrolledStudentCount(Long courseId)` - Get enrollment count

---

### **5. FeeService**
**Location:** `com.collegenet.collegenet.service.FeeService`

**Methods:**
- `Fee assignFeeToStudent(Long studentId, Double totalFee, String academicYear)` - Assign fee
- `Payment payFee(Long studentId, Double amount, Payment.PaymentMode paymentMode)` - Record payment
- `FeeStatusResponse getFeeStatus(Long studentId)` - Get fee status
- `Fee getFeesByStudent(Long studentId)` - Get student fee
- `List<Fee> getAllFees()` - Get all fees
- `Fee createFee(Fee fee)` - Create fee

---

### **6. MarksService**
**Location:** `com.collegenet.collegenet.service.MarksService`

**Methods:**
- `Marks addMarks(Marks marks)` - Add/update marks
- `List<Marks> getMarks(Long studentId, Integer semester)` - Get marks by student and semester
- `List<Marks> getAllMarks()` - Get all marks

---

### **7. ResultService**
**Location:** `com.collegenet.collegenet.service.ResultService`

**Methods:**
- `Result saveResult(Result result)` - Save/update result
- `List<Result> getResultsByStudent(Long studentId)` - Get student results
- `List<Result> getAllResults()` - Get all results

---

### **8. SemesterService**
**Location:** `com.collegenet.collegenet.service.SemesterService`

**Methods:**
- `Semester createSemester(Semester semester)` - Create semester
- `List<Semester> getSemestersByStudent(Long studentId)` - Get student semesters
- `List<Semester> getAllSemesters()` - Get all semesters

---

### **9. ExamService**
**Location:** `com.collegenet.collegenet.service.ExamService`

**Methods:**
- `Exam createExam(Exam exam)` - Create exam
- `List<Exam> getExamsBySemester(Long semesterId)` - Get exams by semester
- `List<Exam> getAllExams()` - Get all exams

---

### **10. CollegeService**
**Location:** `com.collegenet.collegenet.service.CollegeService`

**Methods:**
- `College createCollege(College college)` - Register college
- `College approveCollege(Long collegeId)` - Approve college
- `College getCollegeById(Long collegeId)` - Get college by ID
- `List<College> getAllColleges()` - Get all colleges

---

### **11. AdminService**
**Location:** `com.collegenet.collegenet.service.AdminService`

**Methods:**
- `AdminReportResponse getReports()` - Get aggregated reports
- `List<AdminStudentDTO> getAllStudentsForAdmin()` - Get all students for admin
- `AdminStudentDTO getStudentForAdmin(Long studentId)` - Get student details
- `AdminStudentDTO updateStudent(Long studentId, StudentUpdateDTO updateDTO)` - Update student
- `void deleteStudent(Long studentId)` - Delete student
- `List<CollegeApprovalDTO> getAllCollegesForApproval()` - Get pending colleges
- `CollegeApprovalDTO getCollegeForApproval(Long collegeId)` - Get college approval details
- `CollegeApprovalDTO approveCollege(Long collegeId)` - Approve college
- `CollegeApprovalDTO rejectCollege(Long collegeId)` - Reject college
- `List<College> getAllColleges()` - Get all colleges
- `List<User> getAllUsers()` - Get all users
- `List<Course> getAllCourses()` - Get all courses

---

## Repositories

### **1. UserRepo**
```java
public interface UserRepo extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
```

---

### **2. StudentRepo**
```java
public interface StudentRepo extends JpaRepository<Student, Long> {
    Optional<Student> findByEnrollmentNumber(String enrollmentNumber);
    Optional<Student> findByUserId(Long userId);
}
```

---

### **3. CourseRepo**
```java
public interface CourseRepo extends JpaRepository<Course, Long> {
}
```

---

### **4. StudentCourseRepo**
```java
public interface StudentCourseRepo extends JpaRepository<StudentCourse, Long> {
    List<StudentCourse> findByStudentId(Long studentId);
    List<StudentCourse> findByCourseId(Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<StudentCourse> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM StudentCourse sc WHERE sc.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
}
```

---

### **5. FeeRepo**
```java
public interface FeeRepo extends JpaRepository<Fee, Long> {
    Optional<Fee> findByStudentId(@Param("studentId") Long studentId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Fee f WHERE f.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);
}
```

---

### **6. PaymentRepo**
```java
public interface PaymentRepo extends JpaRepository<Payment, Long> {
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.student.id = :studentId")
    Double sumPaidAmountByStudentId(@Param("studentId") Long studentId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Payment p WHERE p.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
}
```

---

### **7. ResultRepo**
```java
public interface ResultRepo extends JpaRepository<Result, Long> {
    List<Result> findByStudentId(Long studentId);
    Result findTopByStudentIdOrderByIdDesc(Long studentId);
}
```

---

### **8. SemesterRepo**
```java
public interface SemesterRepo extends JpaRepository<Semester, Long> {
    List<Semester> findByStudentId(Long studentId);
}
```

---

### **9. ExamRepo**
```java
public interface ExamRepo extends JpaRepository<Exam, Long> {
    List<Exam> findBySemesterId(Long semesterId);
}
```

---

### **10. MarksRepo**
```java
public interface MarksRepo extends JpaRepository<Marks, Long> {
    List<Marks> findByStudentIdAndSemester(Long studentId, Integer semester);
    List<Marks> findByStudentId(Long studentId);
}
```

---

### **11. CollegeRepo**
```java
public interface CollegeRepo extends JpaRepository<College, Long> {
    Optional<College> findByEmail(String email);
}
```

---

## Configuration & Security

### **SecurityConfig**
**Location:** `com.collegenet.collegenet.config.SecurityConfig`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    // ✅ CORS Configuration
    // Allowed Origins: http://localhost:3000
    // Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
    // Allowed Headers: *
    // Credentials: true
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Features:**
- CSRF disabled for API access
- CORS enabled for frontend on port 3000
- All endpoints permit anonymous access (to be updated with role-based auth if needed)
- BCrypt password encryption

### **Application Configuration**
**File:** `application.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/collegenet_db
    username: postgres
    password: chandan
    hikari:
      connection-timeout: 20000
      maximum-pool-size: 5

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          batch_size: 20
          fetch_size: 50
        order_inserts: true
        order_updates: true

  sql:
    init:
      mode: never
```

**Configuration Details:**
- Server Port: 8080
- Hibernate DDL: Update (auto-create/update schema)
- Batch Processing: Enabled (20 batch size)
- Connection Pool: HikariCP (5 max connections, 20s timeout)

---

## Exception Handling

### **GlobalExceptionHandler**
**Location:** `com.collegenet.collegenet.exception.GlobalExceptionHandler`

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    // Returns: 404 NOT_FOUND
    
    @ExceptionHandler(AuthenticationException.class)
    // Returns: 401 UNAUTHORIZED
    
    @ExceptionHandler(IllegalArgumentException.class)
    // Returns: 400 BAD_REQUEST
    
    @ExceptionHandler(Exception.class)
    // Returns: 500 INTERNAL_SERVER_ERROR
}
```

All exceptions return `ErrorResponse` with:
- `timestamp` - When error occurred
- `status` - HTTP status code
- `error` - Error type
- `message` - Detailed message
- `path` - Request path

---

## Authentication Flow

### **User Registration Flow**
```
1. POST /api/auth/register (RegisterRequest)
   ├── Validate email format
   ├── Check if email exists
   ├── Create User entity with STUDENT role
   ├── Encrypt password using BCrypt
   ├── Save User to database
   └── AUTO-CREATE Student record
       ├── Link User to Student
       ├── Generate enrollment number
       └── Set initial fee status = false

2. Return AuthResponse
   ├── userId
   ├── studentId
   ├── username
   └── message
```

### **User Login Flow**
```
1. POST /api/auth/login (LoginRequest)
   ├── Find user by email
   ├── Validate password using BCrypt
   ├── Check if Student record exists
   └── AUTO-CREATE Student if missing (for STUDENT role)

2. Return AuthResponse
   ├── userId
   ├── studentId
   └── message
```

---

## Database Connection Details

### **Primary Database**
- **Type:** PostgreSQL
- **Host:** localhost
- **Port:** 5432
- **Database Name:** collegenet_db
- **Username:** postgres
- **Password:** chandan

### **Connection Parameters**
```
JDBC URL: jdbc:postgresql://localhost:5432/collegenet_db
Driver: org.postgresql.Driver
```

### **How to Connect in Frontend**

**JavaScript/Node.js (Backend calls):**
```javascript
const BASE_URL = 'http://localhost:8080/api';

// Authentication
const loginResponse = await fetch(`${BASE_URL}/auth/login`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    usernameOrEmail: 'user@example.com',
    password: 'password'
  })
});

const data = await loginResponse.json();
// Use data.userId, data.studentId for subsequent requests
```

**React/Next.js Integration:**
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' }
});

// Make requests
const response = await api.get('/students/1');
const courseData = await api.get('/courses');
```

---

## Summary

This document provides a complete reference for the CollegeNet backend:

✅ **12 Entity Classes** - Complete data models  
✅ **23 DTOs** - Request/response structures  
✅ **11 Controllers** - REST API endpoints  
✅ **11 Services** - Business logic  
✅ **11 Repositories** - Database access  
✅ **11 Database Tables** - Schema design  
✅ **50+ API Endpoints** - Complete API coverage  
✅ **Security Configuration** - CORS & Authentication  
✅ **Exception Handling** - Global error management  

**Ready for Frontend Integration!**

---

**Generated on:** February 18, 2026  
**Backend Framework:** Spring Boot 3.5.9  
**Database:** PostgreSQL  
**Java Version:** 17

