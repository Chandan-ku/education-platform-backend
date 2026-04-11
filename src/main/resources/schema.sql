-- =====================================================
-- College Net Database Schema for PostgreSQL
-- =====================================================

-- Create SEQUENCE for users table
CREATE SEQUENCE IF NOT EXISTS users_seq START 1 INCREMENT 1;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY DEFAULT nextval('users_seq'),
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL CHECK (role IN ('STUDENT', 'COLLEGE', 'ADMIN')),
    phone VARCHAR(20)
);

-- Create SEQUENCE for colleges table
CREATE SEQUENCE IF NOT EXISTS colleges_seq START 1 INCREMENT 1;

-- Colleges Table
CREATE TABLE IF NOT EXISTS colleges (
    id BIGINT PRIMARY KEY DEFAULT nextval('colleges_seq'),
    college_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    approved BOOLEAN DEFAULT FALSE,
    user_id BIGINT UNIQUE,
    CONSTRAINT fk_colleges_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create SEQUENCE for courses table
CREATE SEQUENCE IF NOT EXISTS courses_seq START 1 INCREMENT 1;

-- Courses Table
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT PRIMARY KEY DEFAULT nextval('courses_seq'),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE PRECISION,
    job_guaranteed BOOLEAN,
    duration_in_weeks INTEGER
);

-- Create SEQUENCE for students table
CREATE SEQUENCE IF NOT EXISTS students_seq START 1 INCREMENT 1;

-- Students Table
CREATE TABLE IF NOT EXISTS students (
    id BIGINT PRIMARY KEY DEFAULT nextval('students_seq'),
    user_id BIGINT NOT NULL UNIQUE,
    college_id BIGINT,
    enrollment_number VARCHAR(255) UNIQUE NOT NULL,
    course VARCHAR(255),
    semester INTEGER,
    attendance_percentage DOUBLE PRECISION,
    fees_paid BOOLEAN,
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_students_college FOREIGN KEY (college_id) REFERENCES colleges(id)
);

-- Create SEQUENCE for student_courses table
CREATE SEQUENCE IF NOT EXISTS student_courses_seq START 1 INCREMENT 1;

-- Student Courses Table (Many-to-Many Relationship)
CREATE TABLE IF NOT EXISTS student_courses (
    id BIGINT PRIMARY KEY DEFAULT nextval('student_courses_seq'),
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'DROPPED')),
    CONSTRAINT fk_sc_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_sc_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT uc_student_course UNIQUE (student_id, course_id)
);

-- Create SEQUENCE for semesters table
CREATE SEQUENCE IF NOT EXISTS semesters_seq START 1 INCREMENT 1;

-- Semesters Table
CREATE TABLE IF NOT EXISTS semesters (
    id BIGINT PRIMARY KEY DEFAULT nextval('semesters_seq'),
    semester_number INTEGER,
    academic_year VARCHAR(20),
    student_id BIGINT,
    CONSTRAINT fk_semesters_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- Create SEQUENCE for exams table
CREATE SEQUENCE IF NOT EXISTS exams_seq START 1 INCREMENT 1;

-- Exams Table
CREATE TABLE IF NOT EXISTS exams (
    id BIGINT PRIMARY KEY DEFAULT nextval('exams_seq'),
    exam_type VARCHAR(50),
    subject VARCHAR(255),
    max_marks INTEGER,
    semester_id BIGINT,
    CONSTRAINT fk_exams_semester FOREIGN KEY (semester_id) REFERENCES semesters(id)
);

-- Create SEQUENCE for marks table
CREATE SEQUENCE IF NOT EXISTS marks_seq START 1 INCREMENT 1;

-- Marks Table
CREATE TABLE IF NOT EXISTS marks (
    id BIGINT PRIMARY KEY DEFAULT nextval('marks_seq'),
    student_id BIGINT,
    semester INTEGER,
    subject VARCHAR(255),
    exam_type VARCHAR(50),
    max_marks INTEGER,
    obtained_marks INTEGER,
    pass BOOLEAN
);

-- Create SEQUENCE for fees table
CREATE SEQUENCE IF NOT EXISTS fees_seq START 1 INCREMENT 1;

-- Fees Table
CREATE TABLE IF NOT EXISTS fees (
    id BIGINT PRIMARY KEY DEFAULT nextval('fees_seq'),
    student_id BIGINT NOT NULL,
    total_fee DOUBLE PRECISION NOT NULL,
    academic_year VARCHAR(20),
    CONSTRAINT fk_fees_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- Create SEQUENCE for payments table
CREATE SEQUENCE IF NOT EXISTS payments_seq START 1 INCREMENT 1;

-- Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY DEFAULT nextval('payments_seq'),
    student_id BIGINT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    payment_date DATE NOT NULL,
    payment_mode VARCHAR(50) NOT NULL CHECK (payment_mode IN ('UPI', 'CASH', 'CARD')),
    CONSTRAINT fk_payments_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- Create SEQUENCE for results table
CREATE SEQUENCE IF NOT EXISTS results_seq START 1 INCREMENT 1;

-- Results Table
CREATE TABLE IF NOT EXISTS results (
    id BIGINT PRIMARY KEY DEFAULT nextval('results_seq'),
    status VARCHAR(50),
    student_id BIGINT NOT NULL,
    semester INTEGER,
    sgpa DOUBLE PRECISION,
    cgpa DOUBLE PRECISION,
    passed BOOLEAN,
    CONSTRAINT fk_results_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- Create Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_colleges_email ON colleges(email);
CREATE INDEX IF NOT EXISTS idx_students_enrollment ON students(enrollment_number);
CREATE INDEX IF NOT EXISTS idx_students_user ON students(user_id);
CREATE INDEX IF NOT EXISTS idx_students_college ON students(college_id);
CREATE INDEX IF NOT EXISTS idx_student_courses_student ON student_courses(student_id);
CREATE INDEX IF NOT EXISTS idx_student_courses_course ON student_courses(course_id);
CREATE INDEX IF NOT EXISTS idx_semesters_student ON semesters(student_id);
CREATE INDEX IF NOT EXISTS idx_exams_semester ON exams(semester_id);
CREATE INDEX IF NOT EXISTS idx_marks_student ON marks(student_id);
CREATE INDEX IF NOT EXISTS idx_fees_student ON fees(student_id);
CREATE INDEX IF NOT EXISTS idx_payments_student ON payments(student_id);
CREATE INDEX IF NOT EXISTS idx_results_student ON results(student_id);

