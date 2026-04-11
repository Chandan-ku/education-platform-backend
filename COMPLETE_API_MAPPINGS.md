# Complete API Mappings - Spring Boot Backend

## Project Structure
- **Base URL:** `http://localhost:8080`
- **Database:** PostgreSQL (collegenet_db)
- **Port:** 8080

---

## 1. Authentication Controller
**File:** `AuthController.java`  
**Base URL:** `/api/auth`

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| POST | `/api/auth/register` | Register a new user | `RegisterRequest` | `AuthResponse` |
| POST | `/api/auth/login` | User login | `LoginRequest` | `AuthResponse` |

### DTOs
```json
RegisterRequest {
  email: string,
  password: string,
  fullName: string,
  role: "STUDENT" | "COLLEGE" | "ADMIN"
}

LoginRequest {
  email: string,
  password: string
}

AuthResponse {
  id: number,
  email: string,
  fullName: string,
  role: string,
  token?: string
}
```

---

## 2. User Controller
**File:** `UserController.java`  
**Base URL:** `/api/users`

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| POST | `/api/users` | Create a new user | `User` | `User` |

### Entity
```json
User {
  id: number,
  fullName: string,
  email: string,
  password: string,
  role: "STUDENT" | "COLLEGE" | "ADMIN"
}
```

---

## 3. Student Controller
**File:** `StudentController.java`  
**Base URL:** `/api/students`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/api/students` | Create a new student | `StudentResponseDTO` |
| GET | `/api/students` | Get all students | `List<Student>` |
| GET | `/api/students/{id}` | Get student by ID | `Student` |
| GET | `/api/students/enrollment/{enrollmentNumber}` | Get student by enrollment number | `Student` |
| GET | `/api/students/{studentId}/dashboard` | Get student dashboard data | `StudentDashboardResponse` |

### DTOs
```json
StudentRequestDTO {
  enrollmentNumber: string,
  userId: number,
  collegeId: number,
  course: string,
  semester: number,
  attendancePercentage: number,
  feesPaid: boolean
}

StudentResponseDTO {
  id: number,
  enrollmentNumber: string,
  course: string,
  semester: number,
  attendancePercentage: number,
  feesPaid: boolean
}

StudentDashboardResponse {
  studentId: number,
  studentName: string,
  collegeName: string,
  courseName: string,
  currentSemester: string,
  cgpa: number,
  resultStatus: string,
  totalFee: number,
  paidFee: number,
  dueFee: number
}
```

### Entity
```json
Student {
  id: number,
  user: User,
  college: College,
  enrollmentNumber: string (unique),
  course: string,
  semester: number,
  attendancePercentage: number,
  feesPaid: boolean
}
```

---

## 4. College Controller
**File:** `CollegeController.java`  
**Base URL:** `/api/colleges`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/api/colleges` | Register a new college | `College` |
| GET | `/api/colleges` | Get all colleges | `List<College>` |
| GET | `/api/colleges/{id}` | Get college by ID | `College` |
| PUT | `/api/colleges/{id}/approve` | Approve a college (Admin only) | `College` |

### Entity
```json
College {
  id: number,
  collegeName: string,
  email: string (unique),
  phone: string,
  address: string,
  city: string,
  state: string,
  approved: boolean,
  user: User (college login)
}
```

---

## 5. Course Controller
**File:** `CourseController.java`  
**Base URL:** `/api/courses`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/courses` | Get all courses | `List<CourseDTO>` |
| GET | `/api/courses/{id}` | Get course by ID | `CourseDTO` |
| POST | `/api/courses` | Create a new course | `Course` |
| PUT | `/api/courses/{id}` | Update course | `Course` |
| DELETE | `/api/courses/{id}` | Delete course | `HTTP 200` |
| GET | `/api/courses/college/{collegeId}` | Get courses by college | `List<CourseDTO>` |

### DTOs
```json
CourseDTO {
  id: number,
  title: string,
  description: string,
  price: number,
  jobGuaranteed: boolean,
  durationInWeeks: number
}
```

### Entity
```json
Course {
  id: number,
  title: string,
  description: string,
  price: number,
  jobGuaranteed: boolean,
  durationInWeeks: number
}
```

---

## 6. Enrollment Controller
**File:** `EnrollmentController.java`  
**Base URL:** `/api/enrollments`

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| POST | `/api/enrollments/enroll` | Enroll student to course | `EnrollmentRequest` | `EnrollmentResponse` |
| GET | `/api/enrollments/student/{studentId}/courses` | Get courses for a student | - | `List<CourseDTO>` |
| GET | `/api/enrollments/course/{courseId}/students` | Get students in a course | - | `List<StudentResponseDTO>` |
| PUT | `/api/enrollments/drop` | Drop a course | `DropCourseRequest` | `Map` |
| GET | `/api/enrollments/status/{studentId}/{courseId}` | Check enrollment status | - | `EnrollmentResponse` |

### DTOs
```json
EnrollmentRequest {
  studentId: number,
  courseId: number
}

EnrollmentResponse {
  enrollmentId?: number,
  studentId: number,
  courseId: number,
  enrollmentDate?: string,
  status?: string
}

DropCourseRequest {
  studentId: number,
  courseId: number
}
```

### Entity
```json
StudentCourse {
  id: number,
  student: Student,
  course: Course,
  enrollmentDate: timestamp,
  status: "ACTIVE" | "COMPLETED" | "DROPPED"
}
```

---

## 7. Admin Controller
**File:** `AdminController.java`  
**Base URL:** `/api/admin`

### Dashboard & Reports
| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/admin/colleges` | Get all colleges | `List<College>` |
| GET | `/api/admin/users` | Get all users | `List<User>` |
| GET | `/api/admin/courses` | Get all courses | `List<Course>` |
| GET | `/api/admin/reports` | Get aggregated reports | `AdminReportResponse` |

### Student CRUD
| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/admin/students` | Get all students | `List<AdminStudentDTO>` |
| GET | `/api/admin/students/{studentId}` | Get student by ID | `AdminStudentDTO` |
| PUT | `/api/admin/students/{studentId}` | Update student | `AdminStudentDTO` |
| DELETE | `/api/admin/students/{studentId}` | Delete student | `Map` |

### College Approval Management
| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/admin/colleges/approval/all` | Get all colleges for approval | `List<CollegeApprovalDTO>` |
| GET | `/api/admin/colleges/approval/{collegeId}` | Get college for approval | `CollegeApprovalDTO` |
| PUT | `/api/admin/colleges/{collegeId}/approve` | Approve a college | `CollegeApprovalDTO` |
| PUT | `/api/admin/colleges/{collegeId}/reject` | Reject a college | `CollegeApprovalDTO` |
| GET | `/api/admin/colleges/approval/pending` | Get pending colleges | `List<CollegeApprovalDTO>` |
| GET | `/api/admin/colleges/approval/approved` | Get approved colleges | `List<College>` |

### Enrollment Management
| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/api/admin/enrollments` | Get all enrollments | - | `List<EnrollmentManagementDTO>` |
| GET | `/api/admin/enrollments/student/{studentId}` | Get student enrollments | - | `List<EnrollmentManagementDTO>` |
| GET | `/api/admin/enrollments/course/{courseId}` | Get course enrollments | - | `List<EnrollmentManagementDTO>` |
| PUT | `/api/admin/enrollments/{enrollmentId}/status` | Update enrollment status | `{status: string}` | `EnrollmentManagementDTO` |
| DELETE | `/api/admin/enrollments/{enrollmentId}` | Delete enrollment | - | `Map` |

### Admin DTOs
```json
AdminStudentDTO {
  id: number,
  enrollmentNumber: string,
  studentName: string,
  collegeName: string,
  collegeId: number,
  course: string,
  semester: number,
  attendancePercentage: number,
  feesPaid: boolean,
  email: string,
  phone: string
}

CollegeApprovalDTO {
  collegeId: number,
  collegeName: string,
  email: string,
  phone: string,
  address: string,
  city: string,
  state: string,
  approved: boolean
}

EnrollmentManagementDTO {
  enrollmentId: number,
  studentId: number,
  enrollmentNumber: string,
  studentName: string,
  courseId: number,
  courseTitle: string,
  status: "ACTIVE" | "COMPLETED" | "DROPPED",
  enrollmentDate: string
}

AdminReportResponse {
  totalColleges: number,
  totalUsers: number,
  totalCourses: number,
  totalStudents?: number,
  totalEnrollments?: number
}
```

---

## 8. Fee Controller
**File:** `FeeController.java`  
**Base URL:** `/api/fees`

| Method | Endpoint | Description | Request | Response |
|--------|----------|-------------|---------|----------|
| GET | `/api/fees` | Get all fees | - | `List<Fee>` |
| POST | `/api/fees/assign` | Assign fee to student | `AssignFeeRequest` | `Fee` |
| POST | `/api/fees/pay` | Record a payment | `PaymentRequest` | `Payment` |
| GET | `/api/fees/status/{studentId}` | Get fee status | - | `FeeStatusResponse` |
| GET | `/api/fees/student/{studentId}` | Get student fee | - | `Fee` |
| POST | `/api/fees` | Create fee | `Fee` | `Fee` |

### DTOs
```json
AssignFeeRequest {
  studentId: number,
  totalFee: number,
  academicYear: string
}

PaymentRequest {
  studentId: number,
  amount: number,
  paymentMode: string
}

FeeStatusResponse {
  studentId: number,
  totalFee: number,
  paidFee: number,
  dueFee: number,
  status: string
}
```

### Entities
```json
Fee {
  id: number,
  student: Student,
  totalFee: number,
  academicYear: string
}

Payment {
  id: number,
  student: Student,
  amount: number,
  paymentMode: string,
  paymentDate: timestamp
}
```

---

## 9. Marks Controller
**File:** `MarksController.java`  
**Base URL:** `/api/marks`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/marks` | Get all marks | `List<Marks>` |
| POST | `/api/marks` | Add marks | `Marks` |
| GET | `/api/marks/{studentId}/{semester}` | Get marks by student and semester | `List<Marks>` |

### Entity
```json
Marks {
  id: number,
  student: Student,
  course: Course,
  semester: number,
  score: number
}
```

---

## 10. Result Controller
**File:** `ResultController.java`  
**Base URL:** `/api/results`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/results` | Get all results | `List<Result>` |
| POST | `/api/results` | Upload result | `Result` |
| GET | `/api/results/student/{studentId}` | Get student results | `List<Result>` |

### Entity
```json
Result {
  id: number,
  student: Student,
  semester: number,
  cgpa: number,
  status: string,
  publishedDate: timestamp
}
```

---

## 11. Exam Controller
**File:** `ExamController.java`  
**Base URL:** `/api/exams`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/exams` | Get all exams | `List<Exam>` |
| POST | `/api/exams` | Create exam | `Exam` |
| GET | `/api/exams/semester/{semesterId}` | Get exams by semester | `List<Exam>` |

### Entity
```json
Exam {
  id: number,
  course: Course,
  semester: Semester,
  examDate: date,
  examType: string
}
```

---

## 12. Semester Controller
**File:** `SemesterController.java`  
**Base URL:** `/api/semesters`

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/semesters` | Get all semesters | `List<Semester>` |
| POST | `/api/semesters` | Create semester | `Semester` |
| GET | `/api/semesters/student/{studentId}` | Get semesters by student | `List<Semester>` |

### Entity
```json
Semester {
  id: number,
  semesterNumber: number,
  course: Course,
  startDate: date,
  endDate: date
}
```

---

## Database Schema Summary

### Tables
- **users** - Stores user accounts (STUDENT, COLLEGE, ADMIN)
- **colleges** - College information with approval status
- **students** - Student records linked to users and colleges
- **courses** - Course information
- **semesters** - Semester information for courses
- **student_courses** - Enrollment tracking
- **marks** - Student marks per course per semester
- **results** - Semester results with CGPA
- **exams** - Exam schedules
- **fees** - Fee records for students
- **payments** - Payment records

---

## Error Handling

All endpoints return error responses in the following format:
```json
{
  "error": "Error message",
  "message": "Detailed message"
}
```

### Status Codes
- **200** - OK / Success
- **201** - Created
- **400** - Bad Request
- **404** - Not Found
- **500** - Internal Server Error

---

## Authentication
Currently using basic email/password authentication. JWT tokens recommended for production.

## CORS
Configure CORS in SecurityConfig for frontend communication.

## Database Credentials
- **URL:** jdbc:postgresql://localhost:5432/collegenet_db
- **Username:** postgres
- **Password:** chandan (⚠️ Change in production)

---

**Last Updated:** February 16, 2026  
**Status:** Complete and Ready for Frontend Integration

