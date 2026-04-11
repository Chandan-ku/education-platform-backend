# CollegeNet Database Setup & Configuration Guide

## Problem Description
The application is throwing a `JDBCConnectionException` because it cannot connect to PostgreSQL at `localhost:5432`. The error indicates that the PostgreSQL server is either not running or not accepting connections on the specified port.

## Solution Overview

### Step 1: Install & Start PostgreSQL

#### On Windows:
1. Download PostgreSQL from: https://www.postgresql.org/download/windows/
2. Run the installer and follow the installation wizard
3. Remember the password you set for the `postgres` user (default should be `chandan` as per your config)
4. PostgreSQL typically runs on port 5432

#### Verify PostgreSQL is running:
```powershell
# Check if PostgreSQL service is running on Windows
Get-Service | Where-Object {$_.Name -like '*postgre*'}

# Should show something like: PostgreSQL Database Server 15
```

---

### Step 2: Create the Database

Open PostgreSQL Command Line (`psql`) or pgAdmin and run:

```sql
-- Create the database
CREATE DATABASE collegenet_db;

-- Connect to the database
\c collegenet_db

-- Verify connection
SELECT version();
```

---

### Step 3: Application Configuration

The `application.yml` file has been configured with:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/collegenet_db
    username: postgres
    password: chandan
  jpa:
    hibernate:
      ddl-auto: create-drop  # Drops and creates tables on startup
  sql:
    init:
      mode: always
      platform: postgresql
```

**Configuration Details:**
- `url`: Connection string for PostgreSQL
- `username`: Default PostgreSQL user (postgres)
- `password`: Your PostgreSQL password
- `ddl-auto: create-drop`: Automatically creates tables from entities and drops them on shutdown
- `sql.init.mode: always`: Always initializes the database with schema.sql and data.sql

---

### Step 4: Database Schema & Data Files

The project now includes:

#### `schema.sql`
- Creates all necessary tables with proper constraints
- Sets up sequences for ID generation
- Creates indexes for performance optimization
- Tables included:
  - `users` - User accounts (Admin, College, Student)
  - `colleges` - College information
  - `courses` - Available courses
  - `students` - Student records
  - `student_courses` - Many-to-many relationship
  - `semesters` - Student semesters
  - `exams` - Exam records
  - `marks` - Student marks
  - `fees` - Fee information
  - `payments` - Payment records
  - `results` - Student results

#### `data.sql`
- Populates tables with dummy data for testing
- Sample data includes:
  - 1 Admin user
  - 3 College users
  - 5 Student users
  - 3 Colleges with proper associations
  - 8 Courses
  - 5 Students with enrollments
  - 6 Student-Course enrollments
  - 8 Semesters
  - 6 Exams
  - 9 Marks records
  - 5 Fees entries
  - 8 Payment records
  - 6 Results

---

## Database Tables & Queries

### Users Table
```sql
-- View all users
SELECT id, full_name, email, role FROM users;

-- View users by role
SELECT * FROM users WHERE role = 'STUDENT';
SELECT * FROM users WHERE role = 'COLLEGE';
SELECT * FROM users WHERE role = 'ADMIN';

-- Count users by role
SELECT role, COUNT(*) as count FROM users GROUP BY role;
```

### Colleges Table
```sql
-- View all colleges
SELECT id, college_name, email, city, state, approved FROM colleges;

-- View approved colleges
SELECT * FROM colleges WHERE approved = true;

-- Colleges with user information
SELECT c.college_name, c.email, c.phone, u.full_name 
FROM colleges c 
LEFT JOIN users u ON c.user_id = u.id;
```

### Courses Table
```sql
-- View all courses
SELECT id, title, price, job_guaranteed, duration_in_weeks FROM courses;

-- Courses with job guarantee
SELECT * FROM courses WHERE job_guaranteed = true;

-- Courses sorted by price
SELECT * FROM courses ORDER BY price DESC;

-- Average course price
SELECT AVG(price) as avg_price FROM courses;
```

### Students Table
```sql
-- View all students
SELECT s.id, s.enrollment_number, u.full_name, c.college_name, s.semester, s.fees_paid 
FROM students s 
JOIN users u ON s.user_id = u.id 
JOIN colleges c ON s.college_id = c.id;

-- Students by college
SELECT c.college_name, COUNT(s.id) as student_count 
FROM students s 
JOIN colleges c ON s.college_id = c.id 
GROUP BY c.college_name;

-- Students with fees paid status
SELECT u.full_name, s.enrollment_number, s.fees_paid 
FROM students s 
JOIN users u ON s.user_id = u.id 
WHERE s.fees_paid = true;
```

### Student Courses Table
```sql
-- View student enrollments
SELECT s.enrollment_number, u.full_name, c.title, sc.enrollment_date, sc.status 
FROM student_courses sc 
JOIN students s ON sc.student_id = s.id 
JOIN users u ON s.user_id = u.id 
JOIN courses c ON sc.course_id = c.id;

-- Active enrollments
SELECT * FROM student_courses WHERE status = 'ACTIVE';

-- Completed courses
SELECT * FROM student_courses WHERE status = 'COMPLETED';

-- Students enrolled in specific course
SELECT s.enrollment_number, u.full_name, c.title 
FROM student_courses sc 
JOIN students s ON sc.student_id = s.id 
JOIN users u ON s.user_id = u.id 
JOIN courses c ON sc.course_id = c.id 
WHERE c.title = 'Java Development';
```

### Semesters Table
```sql
-- View all semesters
SELECT s.id, s.semester_number, s.academic_year, u.full_name 
FROM semesters s 
JOIN students st ON s.student_id = st.id 
JOIN users u ON st.user_id = u.id;

-- Semesters by academic year
SELECT academic_year, COUNT(*) as semester_count 
FROM semesters 
GROUP BY academic_year;
```

### Exams Table
```sql
-- View all exams
SELECT id, exam_type, subject, max_marks FROM exams;

-- Exams by type
SELECT exam_type, COUNT(*) as count 
FROM exams 
GROUP BY exam_type;
```

### Marks Table
```sql
-- View all marks
SELECT student_id, semester, subject, exam_type, obtained_marks, pass FROM marks;

-- Passed students
SELECT * FROM marks WHERE pass = true;

-- Failed students
SELECT * FROM marks WHERE pass = false;

-- Average marks by subject
SELECT subject, AVG(obtained_marks) as avg_marks 
FROM marks 
GROUP BY subject;

-- Student-wise marks
SELECT student_id, semester, AVG(obtained_marks) as avg_marks 
FROM marks 
GROUP BY student_id, semester;
```

### Fees Table
```sql
-- View all fees
SELECT f.id, s.enrollment_number, u.full_name, f.total_fee, f.academic_year 
FROM fees f 
JOIN students s ON f.student_id = s.id 
JOIN users u ON s.user_id = u.id;

-- Total fees collected
SELECT SUM(total_fee) as total_fees FROM fees;

-- Fees by academic year
SELECT academic_year, SUM(total_fee) as total 
FROM fees 
GROUP BY academic_year;
```

### Payments Table
```sql
-- View all payments
SELECT p.id, s.enrollment_number, u.full_name, p.amount, p.payment_date, p.payment_mode 
FROM payments p 
JOIN students s ON p.student_id = s.id 
JOIN users u ON s.user_id = u.id;

-- Payments by mode
SELECT payment_mode, COUNT(*) as count, SUM(amount) as total 
FROM payments 
GROUP BY payment_mode;

-- Total amount paid per student
SELECT s.enrollment_number, u.full_name, SUM(p.amount) as total_paid 
FROM payments p 
JOIN students s ON p.student_id = s.id 
JOIN users u ON s.user_id = u.id 
GROUP BY s.id, s.enrollment_number, u.full_name;

-- Payments by date range
SELECT * FROM payments 
WHERE payment_date BETWEEN '2024-01-01' AND '2024-12-31';
```

### Results Table
```sql
-- View all results
SELECT r.id, s.enrollment_number, u.full_name, r.semester, r.sgpa, r.cgpa, r.passed 
FROM results r 
JOIN students s ON r.student_id = s.id 
JOIN users u ON s.user_id = u.id;

-- Passed students
SELECT * FROM results WHERE passed = true;

-- Failed students
SELECT * FROM results WHERE passed = false;

-- Average CGPA
SELECT AVG(cgpa) as avg_cgpa FROM results;

-- Results by semester
SELECT semester, AVG(sgpa) as avg_sgpa, AVG(cgpa) as avg_cgpa 
FROM results 
GROUP BY semester;
```

---

## Troubleshooting Guide

### Issue: "Connection refused" Error
**Solution:**
1. Check if PostgreSQL is installed and running
2. Verify the port (default: 5432)
3. Ensure the database `collegenet_db` exists
4. Check username and password in `application.yml`

### Issue: "Database 'collegenet_db' does not exist"
**Solution:**
```sql
CREATE DATABASE collegenet_db;
```

### Issue: "Permission denied for user 'postgres'"
**Solution:**
- Verify the password in `application.yml` matches your PostgreSQL password
- Reset PostgreSQL password if needed

### Issue: Tables not being created
**Solution:**
- Check that `schema.sql` is in `src/main/resources/`
- Verify `spring.sql.init.mode` is set to `always`
- Check application logs for SQL errors

### Issue: Dummy data not being inserted
**Solution:**
- Check that `data.sql` is in `src/main/resources/`
- Verify foreign key constraints are satisfied
- Check for unique constraint violations

---

## Running the Application

1. **Ensure PostgreSQL is running:**
   ```powershell
   # Start PostgreSQL service
   net start PostgreSQL
   ```

2. **Create the database:**
   ```sql
   CREATE DATABASE collegenet_db;
   ```

3. **Run the Spring Boot application:**
   ```bash
   mvn spring-boot:run
   ```
   Or in your IDE, run `CollegenetApplication.java`

4. **Verify application started:**
   - Check console logs for: "Started CollegenetApplication"
   - Navigate to: http://localhost:8080

5. **Test with API endpoints or database queries**

---

## Database Relationships

```
Users (1) ──→ (1) Colleges
      ├──→ (1) Students
      └──→ Admin/College/Student roles

Colleges (1) ──→ (N) Students

Students (1) ──→ (N) Student_Courses
         ├──→ (N) Semesters
         ├──→ (N) Fees
         ├──→ (N) Payments
         ├──→ (N) Results
         └──→ (N) Marks

Courses (1) ──→ (N) Student_Courses

Semesters (1) ──→ (N) Exams

Marks (N) ──→ (1) Students
```

---

## Summary of Files Modified/Created

1. **application.yml** - Updated with proper SQL initialization
2. **schema.sql** - Comprehensive database schema creation script
3. **data.sql** - Dummy data for testing with all entities

The application will now:
- Automatically create all tables on startup
- Populate them with dummy data
- Be ready for testing and development

