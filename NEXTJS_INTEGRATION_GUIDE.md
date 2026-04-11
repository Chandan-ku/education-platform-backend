# 🚀 Next.js Frontend Integration Guide

## ⚠️ CRITICAL: Copy-Paste These Instructions into Your VS Code Terminal

This guide provides you with complete TypeScript interfaces and service files that match your Spring Boot backend exactly.

---

## STEP 1: Install Required Dependencies

Run this in your `edu-platform` (Next.js project) terminal:

```bash
npm install axios zustand @tanstack/react-query ts-pattern
```

---

## STEP 2: Create API Configuration File

**File:** `src/config/api.config.ts`

```typescript
export const API_CONFIG = {
  BASE_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  TIMEOUT: 30000,
  ENDPOINTS: {
    // Auth
    AUTH: {
      REGISTER: '/api/auth/register',
      LOGIN: '/api/auth/login',
    },
    // Users
    USERS: '/api/users',
    // Students
    STUDENTS: {
      BASE: '/api/students',
      BY_ID: (id: number) => `/api/students/${id}`,
      BY_ENROLLMENT: (enrollment: string) => `/api/students/enrollment/${enrollment}`,
      DASHBOARD: (studentId: number) => `/api/students/${studentId}/dashboard`,
    },
    // Colleges
    COLLEGES: {
      BASE: '/api/colleges',
      BY_ID: (id: number) => `/api/colleges/${id}`,
      APPROVE: (id: number) => `/api/colleges/${id}/approve`,
    },
    // Courses
    COURSES: {
      BASE: '/api/courses',
      BY_ID: (id: number) => `/api/courses/${id}`,
      BY_COLLEGE: (collegeId: number) => `/api/courses/college/${collegeId}`,
    },
    // Enrollments
    ENROLLMENTS: {
      ENROLL: '/api/enrollments/enroll',
      STUDENT_COURSES: (studentId: number) => `/api/enrollments/student/${studentId}/courses`,
      COURSE_STUDENTS: (courseId: number) => `/api/enrollments/course/${courseId}/students`,
      DROP: '/api/enrollments/drop',
      STATUS: (studentId: number, courseId: number) => `/api/enrollments/status/${studentId}/${courseId}`,
    },
    // Admin
    ADMIN: {
      DASHBOARD_COLLEGES: '/api/admin/colleges',
      DASHBOARD_USERS: '/api/admin/users',
      DASHBOARD_COURSES: '/api/admin/courses',
      REPORTS: '/api/admin/reports',
      // Students
      STUDENTS_ALL: '/api/admin/students',
      STUDENTS_BY_ID: (id: number) => `/api/admin/students/${id}`,
      STUDENTS_UPDATE: (id: number) => `/api/admin/students/${id}`,
      STUDENTS_DELETE: (id: number) => `/api/admin/students/${id}`,
      // Colleges
      COLLEGES_APPROVAL_ALL: '/api/admin/colleges/approval/all',
      COLLEGES_APPROVAL_BY_ID: (id: number) => `/api/admin/colleges/approval/${id}`,
      COLLEGES_APPROVE: (id: number) => `/api/admin/colleges/${id}/approve`,
      COLLEGES_REJECT: (id: number) => `/api/admin/colleges/${id}/reject`,
      COLLEGES_PENDING: '/api/admin/colleges/approval/pending',
      COLLEGES_APPROVED: '/api/admin/colleges/approval/approved',
      // Enrollments
      ENROLLMENTS_ALL: '/api/admin/enrollments',
      ENROLLMENTS_BY_STUDENT: (studentId: number) => `/api/admin/enrollments/student/${studentId}`,
      ENROLLMENTS_BY_COURSE: (courseId: number) => `/api/admin/enrollments/course/${courseId}`,
      ENROLLMENTS_UPDATE_STATUS: (enrollmentId: number) => `/api/admin/enrollments/${enrollmentId}/status`,
      ENROLLMENTS_DELETE: (enrollmentId: number) => `/api/admin/enrollments/${enrollmentId}`,
    },
    // Fees
    FEES: {
      BASE: '/api/fees',
      ASSIGN: '/api/fees/assign',
      PAY: '/api/fees/pay',
      STATUS: (studentId: number) => `/api/fees/status/${studentId}`,
      STUDENT: (studentId: number) => `/api/fees/student/${studentId}`,
    },
    // Marks
    MARKS: {
      BASE: '/api/marks',
      BY_STUDENT: (studentId: number, semester: number) => `/api/marks/${studentId}/${semester}`,
    },
    // Results
    RESULTS: {
      BASE: '/api/results',
      BY_STUDENT: (studentId: number) => `/api/results/student/${studentId}`,
    },
    // Exams
    EXAMS: {
      BASE: '/api/exams',
      BY_SEMESTER: (semesterId: number) => `/api/exams/semester/${semesterId}`,
    },
    // Semesters
    SEMESTERS: {
      BASE: '/api/semesters',
      BY_STUDENT: (studentId: number) => `/api/semesters/student/${studentId}`,
    },
  },
};
```

---

## STEP 3: Create TypeScript Interfaces

**File:** `src/types/api.types.ts`

```typescript
// ============================================
// AUTH INTERFACES
// ============================================
export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  role: 'STUDENT' | 'COLLEGE' | 'ADMIN';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  id: number;
  email: string;
  fullName: string;
  role: string;
  token?: string;
}

// ============================================
// USER INTERFACES
// ============================================
export interface User {
  id: number;
  fullName: string;
  email: string;
  password?: string;
  role: 'STUDENT' | 'COLLEGE' | 'ADMIN';
}

// ============================================
// STUDENT INTERFACES
// ============================================
export interface StudentRequestDTO {
  enrollmentNumber: string;
  userId: number;
  collegeId: number;
  course: string;
  semester: number;
  attendancePercentage: number;
  feesPaid: boolean;
}

export interface StudentResponseDTO {
  id: number;
  enrollmentNumber: string;
  course: string;
  semester: number;
  attendancePercentage: number;
  feesPaid: boolean;
}

export interface Student extends StudentResponseDTO {
  user?: User;
  college?: College;
}

export interface StudentDashboardResponse {
  studentId: number;
  studentName: string;
  collegeName: string;
  courseName: string;
  currentSemester: string;
  cgpa: number;
  resultStatus: string;
  totalFee: number;
  paidFee: number;
  dueFee: number;
}

export interface AdminStudentDTO {
  id: number;
  enrollmentNumber: string;
  studentName: string;
  collegeName: string;
  collegeId: number;
  course: string;
  semester: number;
  attendancePercentage: number;
  feesPaid: boolean;
  email: string;
  phone?: string;
}

export interface StudentUpdateDTO {
  course?: string;
  semester?: number;
  attendancePercentage?: number;
  feesPaid?: boolean;
}

// ============================================
// COLLEGE INTERFACES
// ============================================
export interface College {
  id: number;
  collegeName: string;
  email: string;
  phone?: string;
  address?: string;
  city?: string;
  state?: string;
  approved: boolean;
  user?: User;
}

export interface CollegeApprovalDTO {
  collegeId: number;
  collegeName: string;
  email: string;
  phone?: string;
  address?: string;
  city?: string;
  state?: string;
  approved: boolean;
}

// ============================================
// COURSE INTERFACES
// ============================================
export interface Course {
  id: number;
  title: string;
  description?: string;
  price?: number;
  jobGuaranteed?: boolean;
  durationInWeeks?: number;
}

export interface CourseDTO extends Course {}

// ============================================
// ENROLLMENT INTERFACES
// ============================================
export interface EnrollmentRequest {
  studentId: number;
  courseId: number;
}

export interface EnrollmentResponse {
  enrollmentId?: number;
  studentId: number;
  courseId: number;
  enrollmentDate?: string;
  status?: string;
}

export interface DropCourseRequest {
  studentId: number;
  courseId: number;
}

export interface StudentCourse {
  id: number;
  student?: Student;
  course?: Course;
  enrollmentDate?: string;
  status: 'ACTIVE' | 'COMPLETED' | 'DROPPED';
}

export interface EnrollmentManagementDTO {
  enrollmentId: number;
  studentId: number;
  enrollmentNumber: string;
  studentName: string;
  courseId: number;
  courseTitle: string;
  status: 'ACTIVE' | 'COMPLETED' | 'DROPPED';
  enrollmentDate: string;
}

// ============================================
// FEE INTERFACES
// ============================================
export interface AssignFeeRequest {
  studentId: number;
  totalFee: number;
  academicYear: string;
}

export interface PaymentRequest {
  studentId: number;
  amount: number;
  paymentMode: string;
}

export interface Fee {
  id: number;
  student?: Student;
  totalFee: number;
  academicYear: string;
}

export interface Payment {
  id: number;
  student?: Student;
  amount: number;
  paymentMode: string;
  paymentDate?: string;
}

export interface FeeStatusResponse {
  studentId: number;
  totalFee: number;
  paidFee: number;
  dueFee: number;
  status?: string;
}

// ============================================
// MARKS INTERFACES
// ============================================
export interface Marks {
  id: number;
  student?: Student;
  course?: Course;
  semester: number;
  score: number;
}

export interface MarksDTO extends Marks {}

// ============================================
// RESULT INTERFACES
// ============================================
export interface Result {
  id: number;
  student?: Student;
  semester: number;
  cgpa: number;
  status: string;
  publishedDate?: string;
}

export interface ResultDTO extends Result {}

// ============================================
// EXAM INTERFACES
// ============================================
export interface Exam {
  id: number;
  course?: Course;
  semester?: Semester;
  examDate: string;
  examType: string;
}

export interface ExamDTO extends Exam {}

// ============================================
// SEMESTER INTERFACES
// ============================================
export interface Semester {
  id: number;
  semesterNumber: number;
  course?: Course;
  startDate?: string;
  endDate?: string;
}

export interface SemesterDTO extends Semester {}

// ============================================
// ADMIN INTERFACES
// ============================================
export interface AdminReportResponse {
  totalColleges: number;
  totalUsers: number;
  totalCourses: number;
  totalStudents?: number;
  totalEnrollments?: number;
}

// ============================================
// ERROR INTERFACES
// ============================================
export interface ErrorResponse {
  error?: string;
  message?: string;
  status?: number;
}

export interface ApiResponse<T = any> {
  data?: T;
  error?: ErrorResponse;
  status: number;
}
```

---

## STEP 4: Create API Service Client

**File:** `src/services/api.client.ts`

```typescript
import axios, { AxiosInstance, AxiosError, AxiosResponse } from 'axios';
import { API_CONFIG } from '@/config/api.config';
import { ErrorResponse, ApiResponse } from '@/types/api.types';

class ApiClient {
  private axiosInstance: AxiosInstance;

  constructor() {
    this.axiosInstance = axios.create({
      baseURL: API_CONFIG.BASE_URL,
      timeout: API_CONFIG.TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add token to requests if available
    this.axiosInstance.interceptors.request.use((config) => {
      const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Handle responses
    this.axiosInstance.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          // Handle unauthorized
          if (typeof window !== 'undefined') {
            localStorage.removeItem('token');
            window.location.href = '/login';
          }
        }
        return Promise.reject(error);
      }
    );
  }

  async get<T>(url: string, config = {}): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<T> = await this.axiosInstance.get(url, config);
      return { data: response.data, status: response.status };
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  async post<T>(url: string, data?: any, config = {}): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<T> = await this.axiosInstance.post(url, data, config);
      return { data: response.data, status: response.status };
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  async put<T>(url: string, data?: any, config = {}): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<T> = await this.axiosInstance.put(url, data, config);
      return { data: response.data, status: response.status };
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  async delete<T>(url: string, config = {}): Promise<ApiResponse<T>> {
    try {
      const response: AxiosResponse<T> = await this.axiosInstance.delete(url, config);
      return { data: response.data, status: response.status };
    } catch (error) {
      return this.handleError<T>(error);
    }
  }

  private handleError<T>(error: any): ApiResponse<T> {
    const errorResponse: ErrorResponse = {
      message: 'An error occurred',
      status: 500,
    };

    if (error.response) {
      errorResponse.status = error.response.status;
      errorResponse.error = error.response.data?.error || error.response.statusText;
      errorResponse.message = error.response.data?.message || error.message;
    } else if (error.request) {
      errorResponse.message = 'No response from server';
    }

    return { error: errorResponse, status: errorResponse.status || 500 };
  }
}

export const apiClient = new ApiClient();
```

---

## STEP 5: Create Service Methods

### Authentication Service

**File:** `src/services/auth.service.ts`

```typescript
import { API_CONFIG } from '@/config/api.config';
import { apiClient } from './api.client';
import { AuthResponse, LoginRequest, RegisterRequest } from '@/types/api.types';

export const authService = {
  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>(
      API_CONFIG.ENDPOINTS.AUTH.REGISTER,
      data
    );
    
    if (response.data?.token) {
      localStorage.setItem('token', response.data.token);
    }
    
    return response.data || {};
  },

  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>(
      API_CONFIG.ENDPOINTS.AUTH.LOGIN,
      data
    );
    
    if (response.data?.token) {
      localStorage.setItem('token', response.data.token);
    }
    
    return response.data || {};
  },

  logout(): void {
    localStorage.removeItem('token');
  },

  getToken(): string | null {
    return typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  },

  isAuthenticated(): boolean {
    return !!this.getToken();
  },
};
```

### Student Service

**File:** `src/services/student.service.ts`

```typescript
import { API_CONFIG } from '@/config/api.config';
import { apiClient } from './api.client';
import {
  Student,
  StudentDashboardResponse,
  AdminStudentDTO,
  StudentUpdateDTO,
  StudentRequestDTO,
  StudentResponseDTO,
} from '@/types/api.types';

export const studentService = {
  async getAll(): Promise<Student[]> {
    const response = await apiClient.get<Student[]>(API_CONFIG.ENDPOINTS.STUDENTS.BASE);
    return response.data || [];
  },

  async getById(id: number): Promise<Student> {
    const response = await apiClient.get<Student>(
      API_CONFIG.ENDPOINTS.STUDENTS.BY_ID(id)
    );
    return response.data || {};
  },

  async getByEnrollmentNumber(enrollmentNumber: string): Promise<Student> {
    const response = await apiClient.get<Student>(
      API_CONFIG.ENDPOINTS.STUDENTS.BY_ENROLLMENT(enrollmentNumber)
    );
    return response.data || {};
  },

  async getDashboard(studentId: number): Promise<StudentDashboardResponse> {
    const response = await apiClient.get<StudentDashboardResponse>(
      API_CONFIG.ENDPOINTS.STUDENTS.DASHBOARD(studentId)
    );
    return response.data || {};
  },

  async create(data: StudentRequestDTO): Promise<StudentResponseDTO> {
    const response = await apiClient.post<StudentResponseDTO>(
      API_CONFIG.ENDPOINTS.STUDENTS.BASE,
      data
    );
    return response.data || {};
  },

  // Admin methods
  async getAllForAdmin(): Promise<AdminStudentDTO[]> {
    const response = await apiClient.get<AdminStudentDTO[]>(
      API_CONFIG.ENDPOINTS.ADMIN.STUDENTS_ALL
    );
    return response.data || [];
  },

  async getForAdmin(studentId: number): Promise<AdminStudentDTO> {
    const response = await apiClient.get<AdminStudentDTO>(
      API_CONFIG.ENDPOINTS.ADMIN.STUDENTS_BY_ID(studentId)
    );
    return response.data || {};
  },

  async update(studentId: number, data: StudentUpdateDTO): Promise<AdminStudentDTO> {
    const response = await apiClient.put<AdminStudentDTO>(
      API_CONFIG.ENDPOINTS.ADMIN.STUDENTS_UPDATE(studentId),
      data
    );
    return response.data || {};
  },

  async delete(studentId: number): Promise<{ message: string }> {
    const response = await apiClient.delete<{ message: string }>(
      API_CONFIG.ENDPOINTS.ADMIN.STUDENTS_DELETE(studentId)
    );
    return response.data || {};
  },
};
```

### College Service

**File:** `src/services/college.service.ts`

```typescript
import { API_CONFIG } from '@/config/api.config';
import { apiClient } from './api.client';
import { College, CollegeApprovalDTO } from '@/types/api.types';

export const collegeService = {
  async getAll(): Promise<College[]> {
    const response = await apiClient.get<College[]>(API_CONFIG.ENDPOINTS.COLLEGES.BASE);
    return response.data || [];
  },

  async getById(id: number): Promise<College> {
    const response = await apiClient.get<College>(
      API_CONFIG.ENDPOINTS.COLLEGES.BY_ID(id)
    );
    return response.data || {};
  },

  async create(data: College): Promise<College> {
    const response = await apiClient.post<College>(
      API_CONFIG.ENDPOINTS.COLLEGES.BASE,
      data
    );
    return response.data || {};
  },

  async approve(id: number): Promise<College> {
    const response = await apiClient.put<College>(
      API_CONFIG.ENDPOINTS.COLLEGES.APPROVE(id)
    );
    return response.data || {};
  },

  // Admin approval methods
  async getAllForApproval(): Promise<CollegeApprovalDTO[]> {
    const response = await apiClient.get<CollegeApprovalDTO[]>(
      API_CONFIG.ENDPOINTS.ADMIN.COLLEGES_APPROVAL_ALL
    );
    return response.data || [];
  },

  async getForApproval(collegeId: number): Promise<CollegeApprovalDTO> {
    const response = await apiClient.get<CollegeApprovalDTO>(
      API_CONFIG.ENDPOINTS.ADMIN.COLLEGES_APPROVAL_BY_ID(collegeId)
    );
    return response.data || {};
  },

  async approveCollege(collegeId: number): Promise<CollegeApprovalDTO> {
    const response = await apiClient.put<CollegeApprovalDTO>(
      API_CONFIG.ENDPOINTS.ADMIN.COLLEGES_APPROVE(collegeId)
    );
    return response.data || {};
  },

  async rejectCollege(collegeId: number): Promise<CollegeApprovalDTO> {
    const response = await apiClient.put<CollegeApprovalDTO>(
      API_CONFIG.ENDPOINTS.ADMIN.COLLEGES_REJECT(collegeId)
    );
    return response.data || {};
  },

  async getPending(): Promise<CollegeApprovalDTO[]> {
    const response = await apiClient.get<CollegeApprovalDTO[]>(
      API_CONFIG.ENDPOINTS.ADMIN.COLLEGES_PENDING
    );
    return response.data || [];
  },

  async getApproved(): Promise<College[]> {
    const response = await apiClient.get<College[]>(
      API_CONFIG.ENDPOINTS.ADMIN.COLLEGES_APPROVED
    );
    return response.data || [];
  },
};
```

### Course Service

**File:** `src/services/course.service.ts`

```typescript
import { API_CONFIG } from '@/config/api.config';
import { apiClient } from './api.client';
import { Course, CourseDTO } from '@/types/api.types';

export const courseService = {
  async getAll(): Promise<CourseDTO[]> {
    const response = await apiClient.get<CourseDTO[]>(API_CONFIG.ENDPOINTS.COURSES.BASE);
    return response.data || [];
  },

  async getById(id: number): Promise<CourseDTO> {
    const response = await apiClient.get<CourseDTO>(
      API_CONFIG.ENDPOINTS.COURSES.BY_ID(id)
    );
    return response.data || {};
  },

  async getByCollege(collegeId: number): Promise<CourseDTO[]> {
    const response = await apiClient.get<CourseDTO[]>(
      API_CONFIG.ENDPOINTS.COURSES.BY_COLLEGE(collegeId)
    );
    return response.data || [];
  },

  async create(data: Course): Promise<Course> {
    const response = await apiClient.post<Course>(
      API_CONFIG.ENDPOINTS.COURSES.BASE,
      data
    );
    return response.data || {};
  },

  async update(id: number, data: Partial<Course>): Promise<Course> {
    const response = await apiClient.put<Course>(
      API_CONFIG.ENDPOINTS.COURSES.BY_ID(id),
      data
    );
    return response.data || {};
  },

  async delete(id: number): Promise<{ message: string }> {
    const response = await apiClient.delete<{ message: string }>(
      API_CONFIG.ENDPOINTS.COURSES.BY_ID(id)
    );
    return response.data || {};
  },
};
```

### Enrollment Service

**File:** `src/services/enrollment.service.ts`

```typescript
import { API_CONFIG } from '@/config/api.config';
import { apiClient } from './api.client';
import {
  EnrollmentRequest,
  EnrollmentResponse,
  DropCourseRequest,
  CourseDTO,
  StudentResponseDTO,
  EnrollmentManagementDTO,
} from '@/types/api.types';

export const enrollmentService = {
  async enrollStudent(data: EnrollmentRequest): Promise<EnrollmentResponse> {
    const response = await apiClient.post<EnrollmentResponse>(
      API_CONFIG.ENDPOINTS.ENROLLMENTS.ENROLL,
      data
    );
    return response.data || {};
  },

  async getStudentCourses(studentId: number): Promise<CourseDTO[]> {
    const response = await apiClient.get<CourseDTO[]>(
      API_CONFIG.ENDPOINTS.ENROLLMENTS.STUDENT_COURSES(studentId)
    );
    return response.data || [];
  },

  async getCourseStudents(courseId: number): Promise<StudentResponseDTO[]> {
    const response = await apiClient.get<StudentResponseDTO[]>(
      API_CONFIG.ENDPOINTS.ENROLLMENTS.COURSE_STUDENTS(courseId)
    );
    return response.data || [];
  },

  async dropCourse(data: DropCourseRequest): Promise<{ message: string }> {
    const response = await apiClient.put<{ message: string }>(
      API_CONFIG.ENDPOINTS.ENROLLMENTS.DROP,
      data
    );
    return response.data || {};
  },

  async getStatus(
    studentId: number,
    courseId: number
  ): Promise<EnrollmentResponse> {
    const response = await apiClient.get<EnrollmentResponse>(
      API_CONFIG.ENDPOINTS.ENROLLMENTS.STATUS(studentId, courseId)
    );
    return response.data || {};
  },

  // Admin methods
  async getAll(): Promise<EnrollmentManagementDTO[]> {
    const response = await apiClient.get<EnrollmentManagementDTO[]>(
      API_CONFIG.ENDPOINTS.ADMIN.ENROLLMENTS_ALL
    );
    return response.data || [];
  },

  async getByStudent(studentId: number): Promise<EnrollmentManagementDTO[]> {
    const response = await apiClient.get<EnrollmentManagementDTO[]>(
      API_CONFIG.ENDPOINTS.ADMIN.ENROLLMENTS_BY_STUDENT(studentId)
    );
    return response.data || [];
  },

  async getByCourse(courseId: number): Promise<EnrollmentManagementDTO[]> {
    const response = await apiClient.get<EnrollmentManagementDTO[]>(
      API_CONFIG.ENDPOINTS.ADMIN.ENROLLMENTS_BY_COURSE(courseId)
    );
    return response.data || [];
  },

  async updateStatus(
    enrollmentId: number,
    status: string
  ): Promise<EnrollmentManagementDTO> {
    const response = await apiClient.put<EnrollmentManagementDTO>(
      API_CONFIG.ENDPOINTS.ADMIN.ENROLLMENTS_UPDATE_STATUS(enrollmentId),
      { status }
    );
    return response.data || {};
  },

  async delete(enrollmentId: number): Promise<{ message: string }> {
    const response = await apiClient.delete<{ message: string }>(
      API_CONFIG.ENDPOINTS.ADMIN.ENROLLMENTS_DELETE(enrollmentId)
    );
    return response.data || {};
  },
};
```

### Fee Service

**File:** `src/services/fee.service.ts`

```typescript
import { API_CONFIG } from '@/config/api.config';
import { apiClient } from './api.client';
import {
  Fee,
  Payment,
  FeeStatusResponse,
  AssignFeeRequest,
  PaymentRequest,
} from '@/types/api.types';

export const feeService = {
  async getAll(): Promise<Fee[]> {
    const response = await apiClient.get<Fee[]>(API_CONFIG.ENDPOINTS.FEES.BASE);
    return response.data || [];
  },

  async assign(data: AssignFeeRequest): Promise<Fee> {
    const response = await apiClient.post<Fee>(
      API_CONFIG.ENDPOINTS.FEES.ASSIGN,
      data
    );
    return response.data || {};
  },

  async pay(data: PaymentRequest): Promise<Payment> {
    const response = await apiClient.post<Payment>(
      API_CONFIG.ENDPOINTS.FEES.PAY,
      data
    );
    return response.data || {};
  },

  async getStatus(studentId: number): Promise<FeeStatusResponse> {
    const response = await apiClient.get<FeeStatusResponse>(
      API_CONFIG.ENDPOINTS.FEES.STATUS(studentId)
    );
    return response.data || {};
  },

  async getByStudent(studentId: number): Promise<Fee> {
    const response = await apiClient.get<Fee>(
      API_CONFIG.ENDPOINTS.FEES.STUDENT(studentId)
    );
    return response.data || {};
  },

  async create(data: Fee): Promise<Fee> {
    const response = await apiClient.post<Fee>(API_CONFIG.ENDPOINTS.FEES.BASE, data);
    return response.data || {};
  },
};
```

### Marks Service

**File:** `src/services/marks.service.ts`

```typescript
import { API_CONFIG } from '@/config/api.config';
import { apiClient } from './api.client';
import { Marks } from '@/types/api.types';

export const marksService = {
  async getAll(): Promise<Marks[]> {
    const response = await apiClient.get<Marks[]>(API_CONFIG.ENDPOINTS.MARKS.BASE);
    return response.data || [];
  },

  async add(data: Marks): Promise<Marks> {
    const response = await apiClient.post<Marks>(
      API_CONFIG.ENDPOINTS.MARKS.BASE,
      data
    );
    return response.data || {};
  },

  async getByStudent(studentId: number, semester: number): Promise<Marks[]> {
    const response = await apiClient.get<Marks[]>(
      API_CONFIG.ENDPOINTS.MARKS.BY_STUDENT(studentId, semester)
    );
    return response.data || [];
  },
};
```

### Result Service

**File:** `src/services/result.service.ts`

```typescript
import { API_CONFIG } from '@/config/api.config';
import { apiClient } from './api.client';
import { Result } from '@/types/api.types';

export const resultService = {
  async getAll(): Promise<Result[]> {
    const response = await apiClient.get<Result[]>(API_CONFIG.ENDPOINTS.RESULTS.BASE);
    return response.data || [];
  },

  async upload(data: Result): Promise<Result> {
    const response = await apiClient.post<Result>(
      API_CONFIG.ENDPOINTS.RESULTS.BASE,
      data
    );
    return response.data || {};
  },

  async getByStudent(studentId: number): Promise<Result[]> {
    const response = await apiClient.get<Result[]>(
      API_CONFIG.ENDPOINTS.RESULTS.BY_STUDENT(studentId)
    );
    return response.data || [];
  },
};
```

---

## STEP 6: Create Custom Hooks

**File:** `src/hooks/useApi.ts`

```typescript
import { useState, useCallback } from 'react';
import { ApiResponse, ErrorResponse } from '@/types/api.types';

interface UseApiState<T> {
  data: T | null;
  loading: boolean;
  error: ErrorResponse | null;
}

export function useApi<T = any>(
  apiFunction: (...args: any[]) => Promise<T>
) {
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    loading: false,
    error: null,
  });

  const execute = useCallback(
    async (...args: any[]) => {
      setState({ data: null, loading: true, error: null });
      try {
        const result = await apiFunction(...args);
        setState({ data: result, loading: false, error: null });
        return result;
      } catch (error: any) {
        const errorResponse: ErrorResponse = {
          message: error.message || 'An error occurred',
          status: error.status || 500,
        };
        setState({ data: null, loading: false, error: errorResponse });
        throw error;
      }
    },
    [apiFunction]
  );

  return { ...state, execute };
}
```

**File:** `src/hooks/useAuth.ts`

```typescript
import { useState, useCallback, useEffect } from 'react';
import { authService } from '@/services/auth.service';
import { AuthResponse, LoginRequest, RegisterRequest } from '@/types/api.types';

export function useAuth() {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Check if user is already logged in
    const token = authService.getToken();
    if (token) {
      // You might want to validate token here
      setLoading(false);
    } else {
      setLoading(false);
    }
  }, []);

  const login = useCallback(async (credentials: LoginRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await authService.login(credentials);
      setUser(response);
      return response;
    } catch (err: any) {
      const errorMsg = err.message || 'Login failed';
      setError(errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await authService.register(data);
      setUser(response);
      return response;
    } catch (err: any) {
      const errorMsg = err.message || 'Registration failed';
      setError(errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    authService.logout();
    setUser(null);
    setError(null);
  }, []);

  return {
    user,
    loading,
    error,
    isAuthenticated: authService.isAuthenticated(),
    login,
    register,
    logout,
  };
}
```

---

## STEP 7: Update Your .env.local File

**File:** `.env.local`

```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_APP_NAME=EduPlatform
NEXT_PUBLIC_TIMEOUT=30000
```

---

## STEP 8: Example Component with Safe Rendering

**File:** `src/app/admin/users/page.tsx`

```typescript
'use client';

import { useEffect, useState } from 'react';
import { studentService } from '@/services/student.service';
import { AdminStudentDTO } from '@/types/api.types';

export default function AdminUsersPage() {
  const [students, setStudents] = useState<AdminStudentDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStudents = async () => {
      try {
        setLoading(true);
        const data = await studentService.getAllForAdmin();
        setStudents(data);
      } catch (err: any) {
        setError(err?.message || 'Failed to fetch students');
      } finally {
        setLoading(false);
      }
    };

    fetchStudents();
  }, []);

  if (loading) return <div>Loading...</div>;
  if (error) return <div className="text-red-500">Error: {error}</div>;

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6">Students Management</h1>
      
      <div className="overflow-x-auto">
        <table className="w-full border-collapse border border-gray-300">
          <thead className="bg-gray-100">
            <tr>
              <th className="border p-2">ID</th>
              <th className="border p-2">Name</th>
              <th className="border p-2">Email</th>
              <th className="border p-2">Enrollment #</th>
              <th className="border p-2">College</th>
              <th className="border p-2">Course</th>
              <th className="border p-2">Semester</th>
              <th className="border p-2">Fees Paid</th>
            </tr>
          </thead>
          <tbody>
            {students?.length > 0 ? (
              students.map((student) => (
                <tr key={student?.id} className="hover:bg-gray-50">
                  <td className="border p-2">{student?.id}</td>
                  <td className="border p-2">{student?.studentName || 'N/A'}</td>
                  <td className="border p-2">{student?.email || 'N/A'}</td>
                  <td className="border p-2">{student?.enrollmentNumber || 'N/A'}</td>
                  <td className="border p-2">{student?.collegeName || 'N/A'}</td>
                  <td className="border p-2">{student?.course || 'N/A'}</td>
                  <td className="border p-2">{student?.semester || 'N/A'}</td>
                  <td className="border p-2">
                    <span className={student?.feesPaid ? 'text-green-600' : 'text-red-600'}>
                      {student?.feesPaid ? '✓' : '✗'}
                    </span>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={8} className="border p-2 text-center">
                  No students found
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
```

---

## STEP 9: Common Patterns for Your Components

### Safe Property Access Pattern

```typescript
// ✅ SAFE - Uses optional chaining and nullish coalescing
<span>{student?.studentName ?? 'Unknown'}</span>

// ✅ SAFE - Conditional rendering
{student?.feesPaid && <span>Fees Paid</span>}

// ✅ SAFE - Array mapping with optional
{students?.map(s => <div key={s?.id}>{s?.name}</div>)}

// ❌ AVOID - Direct property access
// <span>{student.studentName}</span>

// ❌ AVOID - Without null check
// {students.map(s => ...)} // if students could be undefined
```

### Error Handling Pattern

```typescript
try {
  const data = await someService.fetch();
  setData(data);
} catch (error: any) {
  // Safe error message extraction
  const message = error?.response?.data?.message 
    ?? error?.message 
    ?? 'An error occurred';
  setError(message);
}
```

### Loading State Pattern

```typescript
return (
  <>
    {loading && <Spinner />}
    {error && <ErrorAlert message={error} />}
    {!loading && !error && data && <DataDisplay data={data} />}
    {!loading && !error && !data && <EmptyState />}
  </>
);
```

---

## STEP 10: Testing the Integration

1. **Start Backend:**
   ```bash
   cd C:\Users\gk440\eclipse-workspace-springboot\collegenet
   mvn spring-boot:run
   ```

2. **Start Frontend:**
   ```bash
   cd path/to/edu-platform
   npm run dev
   ```

3. **Test API Endpoints:**
   - Use Postman or Thunder Client
   - Test login: `POST http://localhost:8080/api/auth/login`
   - Test get students: `GET http://localhost:8080/api/students`

---

## 📋 Checklist

- [ ] Created `src/config/api.config.ts`
- [ ] Created `src/types/api.types.ts`
- [ ] Created `src/services/api.client.ts`
- [ ] Created all service files (auth, student, college, etc.)
- [ ] Created `src/hooks/useApi.ts` and `src/hooks/useAuth.ts`
- [ ] Updated `.env.local` with API URL
- [ ] Tested one endpoint manually
- [ ] Verified no `undefined` errors in console
- [ ] All TypeScript interfaces match backend exactly
- [ ] Using safe rendering with `?.` and `??` operators

---

## 🆘 Troubleshooting

### CORS Error
**Problem:** `Access to XMLHttpRequest blocked by CORS`  
**Solution:** Update backend `SecurityConfig.java` to allow CORS:
```java
@Configuration
public class SecurityConfig {
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/api/**");
    }
}
```

### Undefined State
**Problem:** Components showing "undefined" values  
**Solution:** Always use optional chaining:
```typescript
// ❌ Wrong
const name = user.name;

// ✅ Right
const name = user?.name ?? 'Unknown';
```

### Token Not Persisting
**Problem:** Token lost on page refresh  
**Solution:** Add token validation in `useAuth`:
```typescript
useEffect(() => {
  const token = localStorage.getItem('token');
  if (token && isTokenValid(token)) {
    setUser(parseToken(token));
  }
}, []);
```

---

## 📚 API Response Examples

### Success Response
```json
{
  "id": 1,
  "email": "student@example.com",
  "fullName": "John Doe",
  "role": "STUDENT",
  "token": "eyJhbGc..."
}
```

### Error Response
```json
{
  "error": "Invalid credentials",
  "message": "Email or password is incorrect"
}
```

---

**Generated:** February 16, 2026  
**Status:** Ready for Implementation

