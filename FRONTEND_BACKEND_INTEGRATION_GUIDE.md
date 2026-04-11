# CollegeNet Frontend-Backend Integration Guide

**Project**: CollegeNet  
**Backend**: Spring Boot 3.5.9  
**Database**: PostgreSQL  
**Java Version**: 17  
**Base URL**: `http://localhost:8080/api`

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Database Schema](#database-schema)
3. [Entities & Data Models](#entities--data-models)
4. [DTOs (Data Transfer Objects)](#dtos-data-transfer-objects)
5. [API Endpoints](#api-endpoints)
6. [Authentication & Authorization](#authentication--authorization)
7. [Controllers](#controllers)
8. [Services](#services)
9. [Repositories](#repositories)
10. [Exception Handling](#exception-handling)
11. [Configuration](#configuration)
12. [API Request/Response Examples](#api-requestresponse-examples)

---

## 📱 Project Overview

CollegeNet is a comprehensive college management system with the following key features:

- **User Management**: Registration and authentication (Student, College, Admin roles)
- **Student Management**: Student profiles, enrollment tracking, fee management
- **College Management**: College registration and approval workflow
- **Course Management**: Course CRUD operations with filtering capabilities
- **Enrollment**: Student-to-course enrollment management
- **Academic Management**: Marks, Results, Semesters, Exams
- **Fee Management**: Fee assignment, payment tracking, payment history
- **Admin Dashboard**: Reports, approval workflows, comprehensive management views

---

## 🗄️ Database Schema

### PostgreSQL Connection Details
```yaml
URL: jdbc:postgresql://localhost:5432/collegenet_db
Username: postgres
Password: chandan
Dialect: PostgreSQL
```

### Core Tables

#### 1. **users** - User Authentication & Authorization
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) [BCrypt Encrypted],
    role VARCHAR(50) CHECK (role IN ('STUDENT', 'COLLEGE', 'ADMIN')),
    phone VARCHAR(20)
);
```

#### 2. **colleges** - College Registration & Approval
```sql
CREATE TABLE colleges (
    id BIGINT PRIMARY KEY,
    college_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    approved BOOLEAN DEFAULT FALSE,
    user_id BIGINT UNIQUE REFERENCES users(id)
);
```

#### 3. **students** - Student Profiles
```sql
CREATE TABLE students (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    college_id BIGINT NOT NULL REFERENCES colleges(id),
    enrollment_number VARCHAR(255) UNIQUE NOT NULL,
    course VARCHAR(255),
    semester INTEGER,
    attendance_percentage DOUBLE PRECISION,
    fees_paid BOOLEAN
);
```

#### 4. **courses** - Course Catalog
```sql
CREATE TABLE courses (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE PRECISION,
    job_guaranteed BOOLEAN,
    duration_in_weeks INTEGER
);
```

#### 5. **student_courses** - Enrollment Relationship
```sql
CREATE TABLE student_courses (
    id BIGINT PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    enrollment_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE' 
        CHECK (status IN ('ACTIVE', 'COMPLETED', 'DROPPED')),
    UNIQUE(student_id, course_id)
);
```

#### 6. **fees** - Fee Management
```sql
CREATE TABLE fees (
    id BIGINT PRIMARY KEY,
    student_id BIGINT REFERENCES students(id),
    total_amount DOUBLE PRECISION,
    paid_amount DOUBLE PRECISION,
    due_date DATE,
    created_date TIMESTAMP
);
```

#### 7. **payments** - Payment Records
```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY,
    student_id BIGINT REFERENCES students(id),
    fee_id BIGINT REFERENCES fees(id),
    payment_amount DOUBLE PRECISION,
    payment_date TIMESTAMP,
    payment_method VARCHAR(50),
    transaction_id VARCHAR(255)
);
```

#### 8. **marks** - Student Marks
```sql
CREATE TABLE marks (
    id BIGINT PRIMARY KEY,
    student_id BIGINT REFERENCES students(id),
    course_id BIGINT REFERENCES courses(id),
    semester_id BIGINT REFERENCES semesters(id),
    exam_type VARCHAR(50) CHECK (exam_type IN ('INTERNAL', 'MID', 'SEMESTER')),
    obtained_marks DOUBLE PRECISION,
    max_marks DOUBLE PRECISION
);
```

#### 9. **results** - Academic Results
```sql
CREATE TABLE results (
    id BIGINT PRIMARY KEY,
    student_id BIGINT REFERENCES students(id),
    course_id BIGINT REFERENCES courses(id),
    semester_id BIGINT REFERENCES semesters(id),
    sgpa DOUBLE PRECISION,
    cgpa DOUBLE PRECISION,
    total_marks DOUBLE PRECISION,
    grade VARCHAR(5)
);
```

#### 10. **semesters** - Academic Semesters
```sql
CREATE TABLE semesters (
    id BIGINT PRIMARY KEY,
    semester_number INTEGER,
    academic_year VARCHAR(20),
    student_id BIGINT REFERENCES students(id)
);
```

#### 11. **exams** - Exam Schedule
```sql
CREATE TABLE exams (
    id BIGINT PRIMARY KEY,
    exam_type VARCHAR(50),
    subject VARCHAR(255),
    max_marks INTEGER,
    exam_date DATE,
    semester_id BIGINT REFERENCES semesters(id)
);
```

---

## 📊 Entities & Data Models

### 1. **User** Entity
**Package**: `com.collegenet.collegenet.entity`

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
    
    private String password; // BCrypt encrypted
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // STUDENT, COLLEGE, ADMIN
    
    private String phone;
}
```

**Relationships**:
- One-to-One with College
- Referenced by Student (user_id)

---

### 2. **Student** Entity
**Package**: `com.collegenet.collegenet.entity`

```java
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college;
    
    @Column(nullable = false, unique = true)
    private String enrollmentNumber;
    
    private String course;
    private Integer semester;
    private Double attendancePercentage;
    private Boolean feesPaid;
}
```

**Key Fields**:
- `enrollmentNumber`: Auto-generated unique identifier (format: ENR-{timestamp}-{randomId})
- `college_id`: Required - Must be set during student creation
- Linked to User account

---

### 3. **College** Entity
**Package**: `com.collegenet.collegenet.entity`

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
    
    private boolean approved = false; // Admin approval status
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
```

**Approval Workflow**:
- New colleges start with `approved = false`
- Admin must approve via `/api/admin/colleges/{id}/approve`

---

### 4. **Course** Entity
**Package**: `com.collegenet.collegenet.entity`

```java
@Entity
@Table(name = "courses")
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

---

### 5. **StudentCourse** Entity (Enrollment)
**Package**: `com.collegenet.collegenet.entity`

```java
@Entity
@Table(name = "student_courses")
public class StudentCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status; // ACTIVE, COMPLETED, DROPPED
    
    public enum EnrollmentStatus {
        ACTIVE, COMPLETED, DROPPED
    }
}
```

---

### 6. **Fee** Entity
**Package**: `com.collegenet.collegenet.entity`

```java
@Entity
@Table(name = "fees")
public class Fee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    private Double totalAmount;
    private Double paidAmount;
    private LocalDate dueDate;
    private LocalDateTime createdDate;
}
```

---

### 7. **Payment** Entity
**Package**: `com.collegenet.collegenet.entity`

```java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "fee_id")
    private Fee fee;
    
    private Double paymentAmount;
    private LocalDateTime paymentDate;
    private String paymentMethod;
    private String transactionId;
}
```

---

### 8. **Marks** Entity
**Package**: `com.collegenet.collegenet.entity`

```java
@Entity
@Table(name = "marks")
public class Marks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    
    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;
    
    @Enumerated(EnumType.STRING)
    private ExamType examType; // INTERNAL, MID, SEMESTER
    
    private Double obtainedMarks;
    private Double maxMarks;
    
    public enum ExamType {
        INTERNAL, MID, SEMESTER
    }
}
```

---

### 9. **Result** Entity
**Package**: `com.collegenet.collegenet.entity`

```java
@Entity
@Table(name = "results")
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    
    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;
    
    private Double sgpa;
    private Double cgpa;
    private Double totalMarks;
    private String grade;
}
```

---

### 10. **Semester** Entity
**Package**: `com.collegenet.collegenet.entity`

```java
@Entity
@Table(name = "semesters")
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer semesterNumber;
    private String academicYear;
    
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
}
```

---

### 11. **Exam** Entity
**Package**: `com.collegenet.collegenet.entity`

```java
@Entity
@Table(name = "exams")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String examType;
    private String subject;
    private Integer maxMarks;
    private LocalDate examDate;
    
    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;
}
```

---

### 12. **Role** Enum
**Package**: `com.collegenet.collegenet.entity`

```java
public enum Role {
    STUDENT,   // Student user with enrollment capabilities
    COLLEGE,   // College admin managing the college
    ADMIN      // System admin managing everything
}
```

---

## 📦 DTOs (Data Transfer Objects)

DTOs are used for API requests and responses to decouple database entities from API contracts.

### **Authentication DTOs**

#### 1. **RegisterRequest** - User Registration
```java
public class RegisterRequest {
    private String username;      // Full name
    private String email;         // Unique email
    private String password;      // Will be BCrypt encrypted
}
```

#### 2. **LoginRequest** - User Login
```java
public class LoginRequest {
    private String email;
    private String password;
}
```

#### 3. **AuthResponse** - Authentication Response
```java
public class AuthResponse {
    private Long userId;
    private Long studentId;       // Only for STUDENT role (may be null)
    private String username;
    private String message;
}
```

**Example Response**:
```json
{
    "userId": 12,
    "studentId": 19,
    "username": "John Doe",
    "message": "User created successfully"
}
```

---

### **Student DTOs**

#### 1. **StudentRequestDTO** - Create Student
```java
public class StudentRequestDTO {
    private String enrollmentNumber;
    private Long userId;
    private Long collegeId;        // REQUIRED
    private String course;
    private Integer semester;
    private Double attendancePercentage;
    private Boolean feesPaid;
}
```

#### 2. **StudentResponseDTO** - Student Response
```java
public class StudentResponseDTO {
    private Long id;
    private String enrollmentNumber;
    private String course;
    private Integer semester;
    private Double attendancePercentage;
    private Boolean feesPaid;
}
```

#### 3. **StudentDashboardResponse** - Student Dashboard Data
```java
public class StudentDashboardResponse {
    private Long studentId;
    private String studentName;
    private String collegeName;
    private String courseName;
    private Integer currentSemester;
    private Double cgpa;
    private String resultStatus;
    private Double totalFee;
    private Double paidFee;
    private Double dueFee;
}
```

#### 4. **AdminStudentDTO** - Admin Student View
```java
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

#### 5. **StudentUpdateDTO** - Update Student (Admin)
```java
public class StudentUpdateDTO {
    private String course;
    private Integer semester;
    private Double attendancePercentage;
    private Boolean feesPaid;
}
```

---

### **College DTOs**

#### **CollegeApprovalDTO** - College Approval Management
```java
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

---

### **Course DTOs**

#### **CourseDTO** - Course Data
```java
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private Boolean jobGuaranteed;
    private Integer durationInWeeks;
}
```

---

### **Enrollment DTOs**

#### 1. **EnrollmentRequest** - Enroll Student
```java
public class EnrollmentRequest {
    private Long studentId;
    private Long courseId;
}
```

#### 2. **EnrollmentResponse** - Enrollment Response
```java
public class EnrollmentResponse {
    private Long enrollmentId;
    private Long studentId;
    private Long courseId;
    private LocalDate enrollmentDate;
    private String status; // ACTIVE, COMPLETED, DROPPED
}
```

#### 3. **EnrollmentManagementDTO** - Admin Enrollment View
```java
public class EnrollmentManagementDTO {
    private Long enrollmentId;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private LocalDate enrollmentDate;
    private String status;
}
```

#### 4. **DropCourseRequest** - Drop Course
```java
public class DropCourseRequest {
    private Long studentId;
    private Long courseId;
}
```

---

### **Fee DTOs**

#### 1. **AssignFeeRequest** - Assign Fee to Student
```java
public class AssignFeeRequest {
    private Long studentId;
    private Double totalAmount;
    private LocalDate dueDate;
}
```

#### 2. **FeeDTO** - Fee Data
```java
public class FeeDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Double totalAmount;
    private Double paidAmount;
    private Double dueAmount;
    private LocalDate dueDate;
    private LocalDateTime createdDate;
}
```

#### 3. **FeeStatusResponse** - Fee Status
```java
public class FeeStatusResponse {
    private Long feeId;
    private Long studentId;
    private Double totalAmount;
    private Double paidAmount;
    private Double dueAmount;
    private String status; // PAID, PARTIAL, DUE
}
```

#### 4. **PaymentRequest** - Record Payment
```java
public class PaymentRequest {
    private Long studentId;
    private Long feeId;
    private Double paymentAmount;
    private String paymentMethod;
    private String transactionId;
}
```

---

### **Academic DTOs**

#### **MarksDTO** - Marks Data
```java
public class MarksDTO {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Long semesterId;
    private String examType; // INTERNAL, MID, SEMESTER
    private Double obtainedMarks;
    private Double maxMarks;
}
```

#### **ResultDTO** - Result Data
```java
public class ResultDTO {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Long semesterId;
    private Double sgpa;
    private Double cgpa;
    private Double totalMarks;
    private String grade;
}
```

#### **SemesterDTO** - Semester Data
```java
public class SemesterDTO {
    private Long id;
    private Integer semesterNumber;
    private String academicYear;
    private Long studentId;
}
```

#### **ExamDTO** - Exam Data
```java
public class ExamDTO {
    private Long id;
    private String examType;
    private String subject;
    private Integer maxMarks;
    private LocalDate examDate;
    private Long semesterId;
}
```

---

### **Admin DTOs**

#### **AdminReportResponse** - Dashboard Reports
```java
public class AdminReportResponse {
    private Long totalColleges;
    private Long totalStudents;
    private Long totalCourses;
    private Long totalUsers;
    private Long totalPayments;
    private Long totalApprovedColleges;
    private Long totalPendingColleges;
}
```

---

## 🔌 API Endpoints

### **Base URL**: `http://localhost:8080/api`

---

### **Authentication Endpoints** (`/auth`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/auth/register` | RegisterRequest | AuthResponse | Register new user |
| POST | `/auth/login` | LoginRequest | AuthResponse | Login user |

**Request Examples**:

```json
// Register
POST /api/auth/register
{
    "username": "John Doe",
    "email": "john@example.com",
    "password": "password123"
}

// Login
POST /api/auth/login
{
    "email": "john@example.com",
    "password": "password123"
}
```

**Response**:
```json
{
    "userId": 12,
    "studentId": 19,
    "username": "John Doe",
    "message": "Login successful"
}
```

---

### **Student Endpoints** (`/students`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/students` | StudentRequestDTO | StudentResponseDTO | Create student |
| GET | `/students` | - | List<Student> | Get all students |
| GET | `/students/{id}` | - | Student | Get student by ID |
| GET | `/students/enrollment/{enrollmentNumber}` | - | Student | Get by enrollment number |
| GET | `/students/{id}/dashboard` | - | StudentDashboardResponse | Student dashboard data |

---

### **College Endpoints** (`/colleges`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/colleges` | College | College | Register college |
| GET | `/colleges` | - | List<College> | Get all colleges |
| GET | `/colleges/{id}` | - | College | Get college by ID |
| PUT | `/colleges/{id}/approve` | - | College | Approve college (Admin) |

---

### **Course Endpoints** (`/courses`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/courses` | Course | Course | Create course |
| GET | `/courses` | - | List<CourseDTO> | Get all courses |
| GET | `/courses/{id}` | - | CourseDTO | Get course by ID |
| PUT | `/courses/{id}` | Course | Course | Update course |
| DELETE | `/courses/{id}` | - | Message | Delete course |
| GET | `/courses/job-guarantee/{guaranteed}` | - | List<CourseDTO> | Filter by job guarantee |
| GET | `/courses/price?order=asc/desc` | - | List<CourseDTO> | Sort by price |
| GET | `/courses/duration?minWeeks={x}&maxWeeks={y}` | - | List<CourseDTO> | Filter by duration |
| GET | `/courses/search?keyword={term}` | - | List<CourseDTO> | Search courses |

---

### **Enrollment Endpoints** (`/enrollments`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/enrollments/enroll` | EnrollmentRequest | StudentCourse | Enroll student to course |
| GET | `/enrollments/student/{studentId}` | - | List<CourseDTO> | Get student's courses |
| GET | `/enrollments/course/{courseId}` | - | List<StudentResponseDTO> | Get course students |
| PUT | `/enrollments/drop` | DropCourseRequest | Message | Drop course |
| GET | `/enrollments/status/{studentId}/{courseId}` | - | Map<String, Object> | Get enrollment status |
| GET | `/enrollments/course/{courseId}/count` | - | Map<String, Object> | Get enrolled student count |

---

### **Fee Endpoints** (`/fees`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/fees/assign` | AssignFeeRequest | FeeDTO | Assign fee to student |
| GET | `/fees/student/{studentId}` | - | FeeDTO | Get fee by student |
| GET | `/fees/{id}` | - | FeeDTO | Get fee by ID |
| GET | `/fees/status/{studentId}` | - | FeeStatusResponse | Get fee status |
| POST | `/fees/pay` | PaymentRequest | Message | Record payment |
| GET | `/fees/payments/{studentId}` | - | List<Payment> | Get payment history |

---

### **Marks Endpoints** (`/marks`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/marks` | MarksDTO | MarksDTO | Add/Update marks |
| GET | `/marks/student/{studentId}` | - | List<MarksDTO> | Get student marks |
| GET | `/marks/semester/{semesterId}` | - | List<MarksDTO> | Get semester marks |
| GET | `/marks/{id}` | - | MarksDTO | Get marks by ID |

---

### **Result Endpoints** (`/results`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/results` | ResultDTO | ResultDTO | Upload result |
| GET | `/results/student/{studentId}` | - | List<ResultDTO> | Get student results |
| GET | `/results/semester/{semesterId}` | - | List<ResultDTO> | Get semester results |
| GET | `/results/{id}` | - | ResultDTO | Get result by ID |

---

### **Semester Endpoints** (`/semesters`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/semesters` | SemesterDTO | SemesterDTO | Create semester |
| GET | `/semesters` | - | List<SemesterDTO> | Get all semesters |
| GET | `/semesters/student/{studentId}` | - | List<SemesterDTO> | Get student semesters |
| GET | `/semesters/{id}` | - | SemesterDTO | Get semester by ID |

---

### **Exam Endpoints** (`/exams`)

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/exams` | ExamDTO | ExamDTO | Create exam |
| GET | `/exams` | - | List<ExamDTO> | Get all exams |
| GET | `/exams/semester/{semesterId}` | - | List<ExamDTO> | Get exams by semester |
| GET | `/exams/{id}` | - | ExamDTO | Get exam by ID |

---

### **Admin Endpoints** (`/admin`)

#### Dashboard & Reports
| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/admin/colleges` | - | List<College> | Get all colleges |
| GET | `/admin/users` | - | List<User> | Get all users |
| GET | `/admin/courses` | - | List<Course> | Get all courses |
| GET | `/admin/reports` | - | AdminReportResponse | Get admin reports |

#### Student Management
| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/admin/students` | - | List<AdminStudentDTO> | Get all students |
| GET | `/admin/students/{studentId}` | - | AdminStudentDTO | Get student details |
| PUT | `/admin/students/{studentId}` | StudentUpdateDTO | AdminStudentDTO | Update student |
| DELETE | `/admin/students/{studentId}` | - | Message | Delete student |

#### College Approval
| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/admin/colleges/approval/all` | - | List<CollegeApprovalDTO> | Get all colleges |
| GET | `/admin/colleges/approval/{collegeId}` | - | CollegeApprovalDTO | Get college approval details |
| PUT | `/admin/colleges/{collegeId}/approve` | - | CollegeApprovalDTO | Approve college |
| PUT | `/admin/colleges/{collegeId}/reject` | - | CollegeApprovalDTO | Reject college |
| GET | `/admin/colleges/approval/pending` | - | List<CollegeApprovalDTO> | Get pending colleges |
| GET | `/admin/colleges/approval/approved` | - | List<College> | Get approved colleges |

#### Enrollment Management
| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| GET | `/admin/enrollments` | - | List<EnrollmentManagementDTO> | Get all enrollments |
| GET | `/admin/enrollments/student/{studentId}` | - | List<EnrollmentManagementDTO> | Get student enrollments |
| GET | `/admin/enrollments/course/{courseId}` | - | List<EnrollmentManagementDTO> | Get course enrollments |
| PUT | `/admin/enrollments/{enrollmentId}/status` | {status: string} | EnrollmentManagementDTO | Update enrollment status |
| DELETE | `/admin/enrollments/{enrollmentId}` | - | Message | Delete enrollment |

---

## 🔐 Authentication & Authorization

### **Role-Based Access Control (RBAC)**

```
Role: STUDENT
├─ Can view own profile
├─ Can enroll in courses
├─ Can view own marks and results
├─ Can view fees and make payments
└─ Can access student dashboard

Role: COLLEGE
├─ Can register college
├─ Can manage own college details
└─ Can view college-specific data

Role: ADMIN
├─ Can approve/reject colleges
├─ Can view and manage all students
├─ Can view and manage all courses
├─ Can manage all enrollments
├─ Can assign and track fees
├─ Can view system reports
└─ Can delete users/students
```

### **Important Notes**:

1. **Password Encryption**: All passwords are encrypted using BCrypt
2. **Login Response**: Contains `userId` and `studentId` (studentId may be null for COLLEGE/ADMIN roles)
3. **Student Record Creation**: When a user registers with STUDENT role, they need to explicitly create a student profile via `/api/students` with college assignment
4. **Authorization**: Check role from login response and authorize API calls accordingly

---

## 🛠️ Controllers

### **1. AuthController** (`/api/auth`)
- Handles user registration and authentication
- Returns user ID and role information
- Location: `com.collegenet.collegenet.controller.AuthController`

### **2. AdminController** (`/api/admin`)
- Dashboard and reporting
- Student management
- College approval workflow
- Enrollment management
- Location: `com.collegenet.collegenet.controller.AdminController`

### **3. StudentController** (`/api/students`)
- CRUD operations for student profiles
- Student dashboard data
- Location: `com.collegenet.collegenet.controller.StudentController`

### **4. CollegeController** (`/api/colleges`)
- College registration
- College approval (admin)
- Location: `com.collegenet.collegenet.controller.CollegeController`

### **5. CourseController** (`/api/courses`)
- Full CRUD for courses
- Advanced filtering and searching
- Location: `com.collegenet.collegenet.controller.CourseController`

### **6. EnrollmentController** (`/api/enrollments`)
- Enroll students to courses
- Drop courses
- Enrollment status tracking
- Location: `com.collegenet.collegenet.controller.EnrollmentController`

### **7. FeeController** (`/api/fees`)
- Assign fees to students
- Record payments
- Fee status tracking
- Location: `com.collegenet.collegenet.controller.FeeController`

### **8. MarksController** (`/api/marks`)
- Add/update marks
- Retrieve marks by student/semester
- Location: `com.collegenet.collegenet.controller.MarksController`

### **9. ResultController** (`/api/results`)
- Upload/save results
- SGPA/CGPA management
- Location: `com.collegenet.collegenet.controller.ResultController`

### **10. SemesterController** (`/api/semesters`)
- Create and manage semesters
- Link to students
- Location: `com.collegenet.collegenet.controller.SemesterController`

### **11. ExamController** (`/api/exams`)
- Create exams
- Retrieve by semester
- Location: `com.collegenet.collegenet.controller.ExamController`

### **12. UserController** (`/api/users`)
- User creation (internal use)
- Location: `com.collegenet.collegenet.controller.UserController`

---

## 🔧 Services

### **AuthService**
- `AuthResponse register(RegisterRequest)` - Register new user
- `AuthResponse login(LoginRequest)` - Authenticate user
- Password encryption and validation
- **Location**: `com.collegenet.collegenet.service.AuthService`

### **StudentService**
- `StudentResponseDTO createStudent(StudentRequestDTO)` - Create student
- `Student getStudentById(Long)` - Fetch student
- `List<Student> getAllStudents()` - Fetch all students
- `StudentDashboardResponse getStudentDashboard(Long)` - Get dashboard data
- **Location**: `com.collegenet.collegenet.service.StudentService`

### **AdminService**
- `List<AdminStudentDTO> getAllStudentsForAdmin()` - Get all students
- `AdminStudentDTO updateStudent(Long, StudentUpdateDTO)` - Update student
- `void deleteStudent(Long)` - Delete student
- `List<CollegeApprovalDTO> getAllCollegesForApproval()` - Get colleges for approval
- `CollegeApprovalDTO approveCollege(Long)` - Approve college
- `CollegeApprovalDTO rejectCollege(Long)` - Reject college
- `List<EnrollmentManagementDTO> getAllEnrollments()` - Get all enrollments
- `AdminReportResponse getReports()` - Get dashboard reports
- **Location**: `com.collegenet.collegenet.service.AdminService`

### **CourseService**
- `Course createCourse(Course)` - Create course
- `List<Course> getAllCourses()` - Get all courses
- `CourseDTO getCourseDTOById(Long)` - Get course by ID
- `List<CourseDTO> getCoursesByJobGuarantee(Boolean)` - Filter by job guarantee
- `List<CourseDTO> getCoursesSortedByPrice(boolean)` - Sort by price
- `List<CourseDTO> searchCoursesByTitle(String)` - Search courses
- **Location**: `com.collegenet.collegenet.service.CourseService`

### **EnrollmentService**
- `StudentCourse enrollStudentToCourse(Long, Long)` - Enroll student
- `void dropCourse(Long, Long)` - Drop course
- `List<CourseDTO> getStudentCourses(Long)` - Get student courses
- `List<StudentResponseDTO> getCourseStudents(Long)` - Get course students
- **Location**: `com.collegenet.collegenet.service.EnrollmentService`

### **FeeService**
- `FeeDTO assignFee(AssignFeeRequest)` - Assign fee
- `FeeStatusResponse getFeeStatus(Long)` - Get fee status
- `void recordPayment(PaymentRequest)` - Record payment
- `List<Payment> getPaymentHistory(Long)` - Get payment history
- **Location**: `com.collegenet.collegenet.service.FeeService`

### **CollegeService**
- `College createCollege(College)` - Register college
- `List<College> getAllColleges()` - Get all colleges
- `College getCollegeById(Long)` - Get college by ID
- `College approveCollege(Long)` - Approve college
- **Location**: `com.collegenet.collegenet.service.CollegeService`

### **MarksService**
- `MarksDTO addMarks(MarksDTO)` - Add/update marks
- `List<MarksDTO> getStudentMarks(Long)` - Get student marks
- `List<MarksDTO> getSemesterMarks(Long)` - Get semester marks
- **Location**: `com.collegenet.collegenet.service.MarksService`

### **ResultService**
- `ResultDTO uploadResult(ResultDTO)` - Upload result
- `List<ResultDTO> getStudentResults(Long)` - Get student results
- `List<ResultDTO> getSemesterResults(Long)` - Get semester results
- **Location**: `com.collegenet.collegenet.service.ResultService`

### **SemesterService**
- `Semester createSemester(Semester)` - Create semester
- `List<Semester> getSemestersByStudent(Long)` - Get student semesters
- `List<Semester> getAllSemesters()` - Get all semesters
- **Location**: `com.collegenet.collegenet.service.SemesterService`

### **ExamService**
- `Exam createExam(Exam)` - Create exam
- `List<Exam> getExamsBySemester(Long)` - Get exams by semester
- `List<Exam> getAllExams()` - Get all exams
- **Location**: `com.collegenet.collegenet.service.ExamService`

### **UserService**
- `User saveUser(User)` - Save user
- `User findByEmail(String)` - Find user by email
- **Location**: `com.collegenet.collegenet.service.UserService`

---

## 🗃️ Repositories

All repositories extend `JpaRepository<Entity, Long>` from Spring Data JPA.

```java
// UserRepo
public interface UserRepo extends JpaRepository<User, Long> {
    User findByEmail(String email);
}

// StudentRepo
public interface StudentRepo extends JpaRepository<Student, Long> {
    Optional<Student> findByEnrollmentNumber(String enrollmentNumber);
    Optional<Student> findByUserId(Long userId);
}

// StudentCourseRepo
public interface StudentCourseRepo extends JpaRepository<StudentCourse, Long> {
    List<StudentCourse> findByStudentId(Long studentId);
    List<StudentCourse> findByCourseId(Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<StudentCourse> findByStudentIdAndCourseId(Long studentId, Long courseId);
}

// CourseRepo
public interface CourseRepo extends JpaRepository<Course, Long> {
}

// CollegeRepo
public interface CollegeRepo extends JpaRepository<College, Long> {
}

// FeeRepo
public interface FeeRepo extends JpaRepository<Fee, Long> {
    Optional<Fee> findByStudentId(Long studentId);
}

// PaymentRepo
public interface PaymentRepo extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentId(Long studentId);
}

// MarksRepo
public interface MarksRepo extends JpaRepository<Marks, Long> {
    List<Marks> findByStudentId(Long studentId);
    List<Marks> findBySemesterId(Long semesterId);
}

// ResultRepo
public interface ResultRepo extends JpaRepository<Result, Long> {
    List<Result> findByStudentId(Long studentId);
    List<Result> findBySemesterId(Long semesterId);
}

// SemesterRepo
public interface SemesterRepo extends JpaRepository<Semester, Long> {
    List<Semester> findByStudentId(Long studentId);
}

// ExamRepo
public interface ExamRepo extends JpaRepository<Exam, Long> {
    List<Exam> findBySemesterId(Long semesterId);
}
```

**Location**: `com.collegenet.collegenet.repo`

---

## ⚠️ Exception Handling

### **Custom Exceptions**

```java
// ResourceNotFoundException
throw new ResourceNotFoundException("Student not found with id: " + id);

// AuthenticationException
throw new AuthenticationException("Invalid email or password");
```

### **Global Exception Handler**
- Handles all REST API exceptions
- Returns standardized error responses
- Location: `com.collegenet.collegenet.exception`

### **Error Response Format**
```json
{
    "error": "Error message description",
    "timestamp": "2026-03-02T10:30:00Z",
    "status": 400
}
```

---

## ⚙️ Configuration

### **Spring Security Config**
- Location: `com.collegenet.collegenet.config.SecurityConfig`
- Features:
  - CORS configuration for frontend integration
  - CSRF protection
  - Basic authentication support
  - Password encoding (BCrypt)

### **Application Properties**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/collegenet_db
    username: postgres
    password: chandan
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

---

## 📨 API Request/Response Examples

### **1. User Registration**
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "username": "John Doe",
    "email": "john.doe@example.com",
    "password": "SecurePassword123"
}
```

**Response (201 Created)**:
```json
{
    "userId": 1,
    "studentId": null,
    "username": "John Doe",
    "message": "User created successfully"
}
```

---

### **2. User Login**
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "john.doe@example.com",
    "password": "SecurePassword123"
}
```

**Response (200 OK)**:
```json
{
    "userId": 1,
    "studentId": 5,
    "username": "John Doe",
    "message": "Login successful"
}
```

---

### **3. Create Student (After Registration)**
```bash
POST http://localhost:8080/api/students
Content-Type: application/json

{
    "enrollmentNumber": "ENR-1234567890-5678",
    "userId": 1,
    "collegeId": 2,
    "course": "Computer Science",
    "semester": 1,
    "attendancePercentage": 85.0,
    "feesPaid": false
}
```

**Response (201 Created)**:
```json
{
    "id": 5,
    "enrollmentNumber": "ENR-1234567890-5678",
    "course": "Computer Science",
    "semester": 1,
    "attendancePercentage": 85.0,
    "feesPaid": false
}
```

---

### **4. Get Student Dashboard**
```bash
GET http://localhost:8080/api/students/5/dashboard
```

**Response (200 OK)**:
```json
{
    "studentId": 5,
    "studentName": "John Doe",
    "collegeName": "XYZ Engineering College",
    "courseName": "Computer Science",
    "currentSemester": 1,
    "cgpa": 8.5,
    "resultStatus": "PASS",
    "totalFee": 50000,
    "paidFee": 25000,
    "dueFee": 25000
}
```

---

### **5. Get All Courses**
```bash
GET http://localhost:8080/api/courses
```

**Response (200 OK)**:
```json
[
    {
        "id": 1,
        "title": "Java Programming",
        "description": "Learn Java from basics",
        "price": 5000,
        "jobGuaranteed": true,
        "durationInWeeks": 12
    },
    {
        "id": 2,
        "title": "Web Development",
        "description": "Full stack web development",
        "price": 7000,
        "jobGuaranteed": true,
        "durationInWeeks": 16
    }
]
```

---

### **6. Enroll Student in Course**
```bash
POST http://localhost:8080/api/enrollments/enroll
Content-Type: application/json

{
    "studentId": 5,
    "courseId": 1
}
```

**Response (201 Created)**:
```json
{
    "id": 10,
    "studentId": 5,
    "courseId": 1,
    "enrollmentDate": "2026-03-02",
    "status": "ACTIVE"
}
```

---

### **7. Get Admin Reports**
```bash
GET http://localhost:8080/api/admin/reports
```

**Response (200 OK)**:
```json
{
    "totalColleges": 5,
    "totalStudents": 150,
    "totalCourses": 25,
    "totalUsers": 200,
    "totalPayments": 450000,
    "totalApprovedColleges": 3,
    "totalPendingColleges": 2
}
```

---

### **8. Assign Fee to Student**
```bash
POST http://localhost:8080/api/fees/assign
Content-Type: application/json

{
    "studentId": 5,
    "totalAmount": 50000,
    "dueDate": "2026-06-30"
}
```

**Response (201 Created)**:
```json
{
    "id": 1,
    "studentId": 5,
    "studentName": "John Doe",
    "totalAmount": 50000,
    "paidAmount": 0,
    "dueAmount": 50000,
    "dueDate": "2026-06-30",
    "createdDate": "2026-03-02T10:30:00Z"
}
```

---

### **9. Record Payment**
```bash
POST http://localhost:8080/api/fees/pay
Content-Type: application/json

{
    "studentId": 5,
    "feeId": 1,
    "paymentAmount": 25000,
    "paymentMethod": "ONLINE",
    "transactionId": "TXN-123456789"
}
```

**Response (200 OK)**:
```json
{
    "message": "Payment recorded successfully",
    "transactionId": "TXN-123456789",
    "paymentAmount": 25000
}
```

---

### **10. Admin Approve College**
```bash
PUT http://localhost:8080/api/admin/colleges/2/approve
```

**Response (200 OK)**:
```json
{
    "collegeId": 2,
    "collegeName": "ABC College",
    "email": "abc@college.com",
    "phone": "9876543210",
    "address": "123 Main Street",
    "city": "Mumbai",
    "state": "Maharashtra",
    "approved": true
}
```

---

## 🚀 Frontend Implementation Tips

1. **Store Auth Tokens**: After login, store `userId` and `studentId` in localStorage
2. **Role-Based Navigation**: Check `role` to determine which dashboard to show
3. **API Client Setup**: Create an Axios/Fetch interceptor for base URL and error handling
4. **Protected Routes**: Create route guards that check authentication state
5. **Error Handling**: Implement global error handling for 401, 403, 404, 500 errors
6. **College Assignment**: When creating student profile, always provide `collegeId`
7. **Form Validation**: Validate emails, passwords, and required fields before submission
8. **Loading States**: Show loading spinners during API calls
9. **Error Messages**: Display user-friendly error messages from API responses
10. **Refresh Tokens**: Consider implementing JWT tokens for better security

---

## 📞 Important Contacts & Support

- **Database**: PostgreSQL on localhost:5432
- **Backend Server**: http://localhost:8080
- **API Base URL**: http://localhost:8080/api
- **Documentation**: Refer to individual controller JavaDoc comments

---

**Last Updated**: March 2, 2026  
**Version**: 1.0  
**Project Status**: ✅ Ready for Frontend Integration

