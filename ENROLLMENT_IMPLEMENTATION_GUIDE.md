# Student-Course Enrollment Module - Implementation Guide

## Overview
Complete Student-Course enrollment system using a mapping table (StudentCourse entity) instead of string-based course fields. This provides a proper many-to-many relationship through a join table with additional metadata.

---

## Architecture

### 1. Entity Layer

#### StudentCourse Entity
**Location:** `entity/StudentCourse.java`

Maps students to courses with the following fields:
- `id` (Long): Primary key
- `student` (ManyToOne): Reference to Student
- `course` (ManyToOne): Reference to Course
- `enrollmentDate` (LocalDate): Date of enrollment
- `status` (Enum): ACTIVE | COMPLETED | DROPPED

**Key Features:**
- Unique constraint on (student_id, course_id) to prevent duplicate enrollments
- Soft deletion using status DROPPED instead of hard delete
- Lazy loading for performance optimization

---

### 2. Repository Layer

#### StudentCourseRepository
**Location:** `repo/StudentCourseRepository.java`

**Methods:**
```java
List<StudentCourse> findByStudentId(Long studentId)
List<StudentCourse> findByCourseId(Long courseId)
boolean existsByStudentIdAndCourseId(Long studentId, Long courseId)
Optional<StudentCourse> findByStudentIdAndCourseId(Long studentId, Long courseId)
```

---

### 3. Service Layer

#### EnrollmentService
**Location:** `service/EnrollmentService.java`

**Core Methods:**

1. **enrollStudentToCourse(studentId, courseId)**
   - Validates both student and course exist
   - Prevents duplicate enrollment
   - Creates active enrollment with current date
   - Returns StudentCourse entity

2. **getStudentCourses(studentId)**
   - Returns all non-dropped courses for a student
   - Returns List<CourseDTO>
   - Validates student exists

3. **getCourseStudents(courseId)**
   - Returns all active students in a course
   - Returns List<StudentResponseDTO>
   - Validates course exists

4. **dropCourse(studentId, courseId)**
   - Updates enrollment status to DROPPED
   - Soft delete approach (data preserved)
   - Validates enrollment exists

5. **getEnrollmentStatus(studentId, courseId)**
   - Returns current status of enrollment
   - Throws ResourceNotFoundException if not found

6. **getActiveEnrollments(studentId)**
   - Returns only ACTIVE enrollments for a student
   - Used for queries and validations

7. **getEnrolledStudentCount(courseId)**
   - Returns count of active students in a course
   - Useful for capacity management

**Key Patterns:**
- All methods are transactional
- Read-only methods use `@Transactional(readOnly=true)`
- Write methods use `@Transactional` for atomicity
- Constructor injection for all dependencies
- Stream API for filtering and transformations
- Exception handling with ResourceNotFoundException

---

### 4. Controller Layer

#### EnrollmentController
**Location:** `controller/EnrollmentController.java`
**Base Path:** `/api/enrollments`

**REST Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/enrollments/enroll` | Enroll student to course |
| GET | `/api/enrollments/student/{studentId}/courses` | Get all courses for a student |
| GET | `/api/enrollments/course/{courseId}/students` | Get all students in a course |
| PUT | `/api/enrollments/drop` | Drop a course for a student |
| GET | `/api/enrollments/status/{studentId}/{courseId}` | Get enrollment status |
| GET | `/api/enrollments/course/{courseId}/count` | Get enrolled student count |

**Response Examples:**

**1. Enroll Student (POST /api/enrollments/enroll)**
```json
Request:
{
  "studentId": 1,
  "courseId": 5
}

Response (201 Created):
{
  "enrollmentId": 10,
  "studentId": 1,
  "enrollmentNumber": "ENR001",
  "courseId": 5,
  "courseTitle": "Java Programming",
  "enrollmentDate": "2026-01-17",
  "status": "ACTIVE"
}

Error Response (400):
{
  "error": "Student is already enrolled in this course"
}

Error Response (404):
{
  "error": "Student not found with id: 1"
}
```

**2. Get Student Courses (GET /api/enrollments/student/{studentId}/courses)**
```json
Response (200):
[
  {
    "id": 5,
    "title": "Java Programming",
    "description": "Advanced Java concepts",
    "price": 5000.0,
    "jobGuaranteed": true,
    "durationInWeeks": 8
  },
  {
    "id": 6,
    "title": "Spring Boot",
    "description": "Spring Boot framework",
    "price": 6000.0,
    "jobGuaranteed": true,
    "durationInWeeks": 6
  }
]
```

**3. Get Course Students (GET /api/enrollments/course/{courseId}/students)**
```json
Response (200):
[
  {
    "id": 1,
    "enrollmentNumber": "ENR001",
    "course": "CSE",
    "semester": 3,
    "attendancePercentage": 85.5,
    "feesPaid": true
  },
  {
    "id": 2,
    "enrollmentNumber": "ENR002",
    "course": "CSE",
    "semester": 3,
    "attendancePercentage": 90.0,
    "feesPaid": true
  }
]
```

**4. Drop Course (PUT /api/enrollments/drop)**
```json
Request:
{
  "studentId": 1,
  "courseId": 5
}

Response (200):
{
  "message": "Course dropped successfully",
  "studentId": "1",
  "courseId": "5"
}
```

**5. Get Enrollment Status (GET /api/enrollments/status/{studentId}/{courseId})**
```json
Response (200):
{
  "studentId": 1,
  "courseId": 5,
  "status": "ACTIVE"
}
```

**6. Get Student Count (GET /api/enrollments/course/{courseId}/count)**
```json
Response (200):
{
  "courseId": 5,
  "enrolledStudentCount": 25
}
```

---

### 5. DTOs (Data Transfer Objects)

#### EnrollmentRequest
**Location:** `dto/EnrollmentRequest.java`
```java
{
  Long studentId,
  Long courseId
}
```

#### EnrollmentResponse
**Location:** `dto/EnrollmentResponse.java`
```java
{
  Long enrollmentId,
  Long studentId,
  String enrollmentNumber,
  Long courseId,
  String courseTitle,
  LocalDate enrollmentDate,
  EnrollmentStatus status
}
```

#### DropCourseRequest
**Location:** `dto/DropCourseRequest.java`
```java
{
  Long studentId,
  Long courseId
}
```

---

## Design Patterns Used

1. **Layered Architecture**
   - Clean separation between controller, service, and repository layers
   - Controllers remain thin (routing & validation only)
   - Services contain all business logic

2. **DTO Pattern**
   - Never expose entities directly in API responses
   - DTOs provide contract between API and clients
   - Easy to update API without changing entities

3. **Constructor Injection**
   - All dependencies injected via constructor
   - Immutable, testable, and explicit dependencies
   - Uses Lombok `@RequiredArgsConstructor`

4. **Transaction Management**
   - `@Transactional` for write operations
   - `@Transactional(readOnly=true)` for queries
   - Ensures data consistency and isolation

5. **Stream API**
   - Functional programming for filtering and transformations
   - Clean, readable code for list operations

6. **Soft Delete**
   - Uses status field instead of hard delete
   - Preserves data for auditing and reporting
   - Still filters out deleted records in queries

---

## Error Handling

**Exception Types:**
- `ResourceNotFoundException`: When student/course/enrollment not found
- `IllegalArgumentException`: When duplicate enrollment attempted

**HTTP Status Codes:**
- `201 Created`: Successful enrollment
- `200 OK`: Successful GET, PUT operations
- `400 Bad Request`: Invalid input (duplicate enrollment)
- `404 Not Found`: Student, course, or enrollment not found

---

## Database Schema

```sql
CREATE TABLE student_courses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  enrollment_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  FOREIGN KEY (student_id) REFERENCES students(id),
  FOREIGN KEY (course_id) REFERENCES courses(id),
  UNIQUE KEY (student_id, course_id)
);
```

---

## Usage Examples

### 1. Enroll a Student
```bash
curl -X POST http://localhost:8080/api/enrollments/enroll \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "courseId": 5
  }'
```

### 2. View Student's Courses
```bash
curl http://localhost:8080/api/enrollments/student/1/courses
```

### 3. View Course's Students
```bash
curl http://localhost:8080/api/enrollments/course/5/students
```

### 4. Drop a Course
```bash
curl -X PUT http://localhost:8080/api/enrollments/drop \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "courseId": 5
  }'
```

### 5. Check Enrollment Status
```bash
curl http://localhost:8080/api/enrollments/status/1/5
```

### 6. Get Course Enrollment Count
```bash
curl http://localhost:8080/api/enrollments/course/5/count
```

---

## Best Practices Implemented

✅ **Do NOT expose entities** - All APIs use DTOs
✅ **Constructor injection** - All dependencies via constructor
✅ **Transactional methods** - Proper transaction management
✅ **Input validation** - Checks for null and existence
✅ **Stream API** - Functional approach to filtering
✅ **Soft deletes** - Data preservation via status
✅ **Meaningful exceptions** - Clear error messages
✅ **REST conventions** - Proper HTTP methods and status codes
✅ **Read-only transactions** - Optimized for queries
✅ **Lazy loading** - Performance optimization with FetchType.LAZY

---

## Future Enhancements

1. Add pagination for large result sets
2. Add filtering (by status, date range)
3. Add sorting capabilities
4. Add audit logging for enrollment changes
5. Add notification system when enrolled
6. Add validation for course capacity limits
7. Add academic calendar integration
8. Add grade tracking per enrollment
9. Add withdrawal with refund calculation
10. Add enrollment history/timeline

