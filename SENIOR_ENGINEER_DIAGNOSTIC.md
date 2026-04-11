# CollegeNet Database & Authentication System - Senior Engineer Diagnostic & Fix Guide

**Date**: February 1, 2026  
**Status**: Production Debug Session  
**Severity**: Critical (Multiple interconnected issues)

---

## EXECUTIVE SUMMARY

You have **4 interconnected but separately fixable problems**:

1. **Foreign Key Constraint Errors** → Design issue in cascade strategy
2. **Login returning studentId=null** → Mapping gap (User ↛ Student relationship incomplete)
3. **Dashboard API receiving "null" studentId** → Frontend not passing ID correctly
4. **Wrong endpoint path interpretation** → Path variable handling issue

**Root Cause**: Users exist but have NO corresponding Student records. The relationship is ONE-TO-MANY (Users can have multiple Student records), but auth system expects ONE-TO-ONE.

---

## PROBLEM #1: Foreign Key Constraint Errors

### Why This Happens

```
USER TABLE STRUCTURE:
┌──────────┐
│ users    │  id, email, role
│ (parent) │
└────┬─────┘
     │ (OneToOne)
     │ user_id (unique)
     │
┌────▼─────────────────────┐
│ colleges (parent)         │
│ id, email, user_id        │
└────┬──────────────────────┘
     │ (OneToOne)
     │ user_id (unique)
     │
     X ERROR: Can't reference same User twice!
     │ (ManyToOne)
     │
┌────▼──────────────────────────┐
│ students                       │
│ id, user_id (unique), college_id │
└────────────────────────────────┘
```

**Current Entity Relationships** (from code):
- `User` ← no foreign key definition
- `College.user_id` → unique reference to ONE User
- `Student.user_id` → unique reference to ONE User

**The Problem**: 
- A User can be referenced by BOTH College.user_id AND Student.user_id
- But both are UNIQUE constraints
- When you try to delete/update User → violates FK constraint on both

### Correct Data Model

**FOR A PRODUCTION SYSTEM**, you should have **ONE OF THESE**:

#### Option A: User → Role-Based Child (Recommended)
```
Users (base)
├── User role = COLLEGE → links to Colleges table
├── User role = STUDENT → links to Students table
└── User role = ADMIN → no child link
```

#### Option B: Polymorphic Hierarchy (Advanced)
```
Users (discriminator: role)
├── CollegeUser → College
├── StudentUser → Student
└── AdminUser → no child
```

### IMMEDIATE FIX: Add Cascade Delete & Orphan Removal

Edit `College.java`:
```java
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "user_id")
private User user;
```

Edit `Student.java`:
```java
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "user_id", nullable = false, unique = true)
private User user;
```

**BUT** this still won't work because User can't be cascade-deleted from both directions.

### PROPER FIX: Conditional Deletion Logic (Safe Production Approach)

Create a `UserDeletionService`:

```java
// Add to UserService.java
@Transactional
public void deleteUserWithCascade(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    
    // 1. If user is a college, delete college first
    College college = collegeRepository.findByUserId(userId).orElse(null);
    if (college != null) {
        collegeRepository.delete(college);
    }
    
    // 2. If user is a student, delete student first
    Student student = studentRepository.findByUserId(userId).orElse(null);
    if (student != null) {
        // Delete associated records first
        paymentRepository.deleteAllByStudentId(student.getId());
        feeRepository.deleteByStudentId(student.getId());
        resultRepository.deleteAllByStudentId(student.getId());
        marksRepository.deleteAllByStudentId(student.getId());
        studentCourseRepository.deleteAllByStudentId(student.getId());
        // Finally, delete the student
        studentRepository.delete(student);
    }
    
    // 3. Delete the user
    userRepository.delete(user);
}
```

### Deletion Order (Manual, Safe, Production-Ready)

```sql
-- 1. Delete leaf records first (no dependents)
DELETE FROM payments WHERE student_id = ?;
DELETE FROM marks WHERE student_id = ?;
DELETE FROM results WHERE student_id = ?;
DELETE FROM student_courses WHERE student_id = ?;
DELETE FROM fees WHERE student_id = ?;
DELETE FROM semesters WHERE student_id = ?;

-- 2. Delete parent records (single dependent)
DELETE FROM students WHERE user_id = ? OR college_id = ?;
DELETE FROM colleges WHERE user_id = ?;

-- 3. Finally, delete user
DELETE FROM users WHERE id = ?;
```

---

## PROBLEM #2: Login Returns studentId = null

### Root Cause Analysis

```
AuthService.login() flow:
┌─────────────────────────────────────────────┐
│ 1. Find User by email                       │ ✅ Works
│    SELECT * FROM users WHERE email = ?     │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│ 2. Fetch Student by user_id                │ ❌ FAILS
│    SELECT * FROM students                  │
│    WHERE user_id = ?                       │
└─────────────────────────────────────────────┘
                    ↓
          User exists but NO Student record
                    ↓
           studentId = null returned
```

**Why studentId is null:**
- User table has the login record
- Student table is EMPTY for that user_id
- `studentRepo.findByUserId(userId)` returns `Optional.empty()`

### The Real Issue: Student Creation Flow

When you register a new student user:

**Current Code** (AuthService.register):
```java
User saved = userRepo.save(user);  // ✅ User created
Long studentId = null;
Student student = studentRepo.findByUserId(saved.getId()).orElse(null);
if (student != null) {
    studentId = student.getId();  // ❌ student is STILL null!
}
return new AuthResponse(saved.getId(), studentId, ...);
```

**Missing Step**: User registration creates a User but NEVER creates a Student record!

### Fix #2A: Register Should Create Student Automatically

```java
// In AuthService.java
public AuthResponse register(RegisterRequest request) {
    if (userRepo.findByEmail(request.getEmail()) != null) {
        throw new AuthenticationException("Email is already in use");
    }
    
    // 1. Create User
    User user = new User();
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(Role.STUDENT);
    User savedUser = userRepo.save(user);
    
    // 2. AUTO-CREATE Student record for STUDENT role
    Long studentId = null;
    if (savedUser.getRole() == Role.STUDENT) {
        Student student = new Student();
        student.setUser(savedUser);
        // Require college_id from request? Or default?
        // For now, mark as required in StudentRegistrationRequest
        student.setEnrollmentNumber(generateEnrollmentNumber());
        Student savedStudent = studentRepo.save(student);
        studentId = savedStudent.getId();
    }
    
    return new AuthResponse(savedUser.getId(), studentId, 
                           savedUser.getEmail(), "User registered successfully");
}

private String generateEnrollmentNumber() {
    return "ENR-" + System.currentTimeMillis();
}
```

### Fix #2B: Database Check - Are Student Records Existing?

Run this SQL to diagnose:

```sql
-- Check Users without corresponding Student records
SELECT u.id, u.email, u.role, s.id as student_id
FROM users u
LEFT JOIN students s ON u.id = s.user_id
WHERE u.role = 'STUDENT' AND s.id IS NULL;

-- If this returns rows → those users have no student records
-- Solution: Insert Student records for them

INSERT INTO students (user_id, college_id, enrollment_number, fees_paid)
SELECT u.id, 1, 'ENR-' || u.id, false
FROM users u
LEFT JOIN students s ON u.id = s.user_id
WHERE u.role = 'STUDENT' AND s.id IS NULL;
```

---

## PROBLEM #3: Dashboard API Error - "Failed to convert 'String' to 'Long'"

### Error Analysis

```
Error: MethodArgumentTypeMismatchException: 
Failed to convert value of type 'String' to required type 'Long'
For input string: "null"
```

This means:
- Frontend is passing `?studentId=null` (as STRING)
- Backend expects `Long` (numeric type)
- Spring can't convert string "null" to Long

### Frontend Problem - How studentId Becomes "null"

**Scenario 1: Missing from localStorage**
```typescript
// Current code in dashboard
const { studentId, isLoading } = useAuth();  // ← Returns null
fetch(`/api/students/${studentId}/dashboard`)  // ← Becomes "/api/students/null/dashboard"
```

**Scenario 2: useAuth hook not implemented correctly**
```typescript
// hooks/useAuth.ts might be:
export function useAuth() {
    const [studentId, setStudentId] = useState<number | null>(null);
    
    useEffect(() => {
        const id = localStorage.getItem('studentId');
        setStudentId(id ? parseInt(id) : null);  // ← Still null if localStorage empty
    }, []);
    
    return { studentId, isLoading: false };
}
```

### CORRECT Fix: Ensure studentId is NEVER null Before API Call

```typescript
// hooks/useAuth.ts
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

interface AuthData {
    userId: number;
    studentId: number;
    username: string;
}

export function useAuth() {
    const [auth, setAuth] = useState<AuthData | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const router = useRouter();

    useEffect(() => {
        const userId = localStorage.getItem('userId');
        const studentId = localStorage.getItem('studentId');
        const username = localStorage.getItem('username');

        if (userId && studentId && username) {
            setAuth({
                userId: parseInt(userId),
                studentId: parseInt(studentId),
                username,
            });
        } else {
            // Not authenticated
            setAuth(null);
        }
        setIsLoading(false);
    }, []);

    return { auth, isLoading };
}
```

**In Dashboard Page**:

```typescript
// app/student/dashboard/page.tsx
'use client';

import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import { useEffect, useState } from 'react';

interface StudentDashboard {
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

export default function StudentDashboardPage() {
    const router = useRouter();
    const { auth, isLoading } = useAuth();
    const [dashboard, setDashboard] = useState<StudentDashboard | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isLoading) return;  // Wait for auth to load

        if (!auth?.studentId) {
            // Not authenticated, redirect to login
            router.push('/login');
            return;
        }

        // ✅ NOW we have a valid studentId
        const fetchDashboard = async () => {
            setLoading(true);
            try {
                const response = await fetch(
                    `http://localhost:8080/api/students/${auth.studentId}/dashboard`,
                    {
                        method: 'GET',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                    }
                );

                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }

                const data: StudentDashboard = await response.json();
                setDashboard(data);
            } catch (err) {
                setError(err instanceof Error ? err.message : 'Unknown error');
            } finally {
                setLoading(false);
            }
        };

        fetchDashboard();
    }, [auth, isLoading, router]);

    if (isLoading) return <div>Authenticating...</div>;
    if (error) return <div style={{ color: 'red' }}>Error: {error}</div>;
    if (!dashboard) return <div>Loading dashboard...</div>;

    return (
        <main style={{ padding: '20px' }}>
            <h1>Welcome, {dashboard.studentName}</h1>
            <section style={{ display: 'grid', gap: '20px', gridTemplateColumns: 'repeat(2, 1fr)' }}>
                <div>
                    <label>Enrollment Number</label>
                    <p>{dashboard.studentId}</p>
                </div>
                <div>
                    <label>College</label>
                    <p>{dashboard.collegeName}</p>
                </div>
                <div>
                    <label>Course</label>
                    <p>{dashboard.courseName}</p>
                </div>
                <div>
                    <label>Semester</label>
                    <p>{dashboard.currentSemester}</p>
                </div>
                <div>
                    <label>CGPA</label>
                    <p>{dashboard.cgpa?.toFixed(2) || 'N/A'}</p>
                </div>
                <div>
                    <label>Status</label>
                    <p>{dashboard.resultStatus || 'Pending'}</p>
                </div>
                <div>
                    <label>Total Fees</label>
                    <p>₹{dashboard.totalFee?.toLocaleString() || '0'}</p>
                </div>
                <div>
                    <label>Paid</label>
                    <p>₹{dashboard.paidFee?.toLocaleString() || '0'}</p>
                </div>
                <div>
                    <label>Due</label>
                    <p style={{ color: dashboard.dueFee > 0 ? 'red' : 'green' }}>
                        ₹{dashboard.dueFee?.toLocaleString() || '0'}
                    </p>
                </div>
            </section>
        </main>
    );
}
```

---

## PROBLEM #4: Backend Endpoint - Correct Implementation

### Current StudentController

```java
@GetMapping("/{studentId}/dashboard")
public ResponseEntity<StudentDashboardResponse> getDashboard(
        @PathVariable Long studentId) {
    return ResponseEntity.ok(
            studentService.getStudentDashboard(studentId)
    );
}
```

**This is CORRECT** ✅. The issue is NOT here.

### Proper Error Handling - Add to StudentController

```java
@GetMapping("/{studentId}/dashboard")
public ResponseEntity<StudentDashboardResponse> getDashboard(
        @PathVariable Long studentId) {
    
    if (studentId == null || studentId <= 0) {
        return ResponseEntity
            .badRequest()
            .body(null);  // Or throw exception
    }
    
    try {
        StudentDashboardResponse response = studentService.getStudentDashboard(studentId);
        return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(null);
    }
}
```

**Better**: Add GlobalExceptionHandler (already exists in your code, good!)

```java
// GlobalExceptionHandler.java - add this:
@ExceptionHandler(MethodArgumentTypeMismatchException.class)
public ResponseEntity<ErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex) {
    
    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Invalid Parameter Type")
        .message("Invalid " + ex.getName() + ": " + ex.getValue())
        .path(ex.getPropertyName())
        .build();
    
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

---

## COMPLETE FIXED DATA FLOW

### Login → Dashboard Flow (Corrected)

```
Step 1: User Logs In
┌──────────────────────────────────────────┐
│ Frontend: POST /api/auth/login           │
│ Body: { email: "user@test.com", pwd }   │
└──────────────────────────────────────────┘
         ↓
┌──────────────────────────────────────────────────┐
│ Backend: AuthService.login()                    │
├──────────────────────────────────────────────────┤
│ 1. Find User by email (userRepo)       ✅       │
│ 2. Verify password                      ✅       │
│ 3. Fetch Student by user_id (FIX!)     ⭐       │
│    - Must check if Student exists               │
│    - If NOT → create it OR return error         │
│ 4. Build AuthResponse with studentId   ✅       │
└──────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────┐
│ Frontend: Receives Response                    │
│ {                                              │
│   "userId": 5,                                 │
│   "studentId": 10,   ← THIS IS KEY             │
│   "username": "user@test.com"                  │
│ }                                              │
└────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────┐
│ Frontend: Store in localStorage                │
│ localStorage.setItem('studentId', '10');  ✅   │
│ localStorage.setItem('userId', '5');      ✅   │
│ localStorage.setItem('username', 'user...') ✅ │
└────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────┐
│ Frontend: Navigate to Dashboard                │
│ router.push('/student/dashboard');         ✅  │
└────────────────────────────────────────────────┘

Step 2: Dashboard Page Loads
┌────────────────────────────────────────────────┐
│ useAuth() Hook Runs                            │
├────────────────────────────────────────────────┤
│ 1. Read from localStorage                  ✅  │
│    const id = localStorage.getItem(...)       │
│ 2. Parse to number                        ✅  │
│    setAuth({studentId: parseInt(id)})        │
└────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────┐
│ Fetch Dashboard Data                           │
│ GET /api/students/10/dashboard            ✅  │
│ (studentId is NOW a number, not "null")       │
└────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────┐
│ Backend: StudentController.getDashboard()      │
│ @PathVariable Long studentId = 10         ✅  │
│ 1. Fetch Student(id=10)                   ✅  │
│ 2. Fetch Result for student               ✅  │
│ 3. Calculate fees                         ✅  │
│ 4. Build StudentDashboardResponse         ✅  │
└────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────┐
│ Response: 200 OK                               │
│ {                                              │
│   "studentId": 10,                             │
│   "studentName": "Raj Kumar",                  │
│   "collegeName": "IIT Delhi",                  │
│   "courseName": "CSE",                         │
│   "currentSemester": "Semester 3",             │
│   "cgpa": 8.5,                                 │
│   "totalFee": 150000.0,                        │
│   "paidFee": 75000.0,                          │
│   "dueFee": 75000.0                            │
│ }                                              │
└────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────┐
│ Dashboard UI Renders Successfully          ✅  │
│ Shows all student information                  │
└────────────────────────────────────────────────┘
```

---

## IMPLEMENTATION CHECKLIST

### Backend Fixes

- [ ] **Fix #1: AuthService.register() - Create Student automatically**
  - File: `src/main/java/.../service/AuthService.java`
  - Add: Student creation logic in register method
  - Add: generateEnrollmentNumber() method

- [ ] **Fix #2: Add UserDeletionService - Safe cascade deletes**
  - File: `src/main/java/.../service/UserService.java`
  - Add: deleteUserWithCascade(Long userId) method

- [ ] **Fix #3: Update GlobalExceptionHandler - Handle type mismatch**
  - File: `src/main/java/.../exception/GlobalExceptionHandler.java`
  - Add: handleTypeMismatch() method

- [ ] **Fix #4: Update StudentService.getStudentDashboard() - Add null checks**
  - Add: Proper error handling for null student
  - Add: Default values for optional fields

### Frontend Fixes

- [ ] **Fix #5: Create/Update useAuth hook**
  - File: `hooks/useAuth.ts` (create if not exists)
  - Returns: { auth: AuthData | null, isLoading: boolean }
  - Logic: Read from localStorage, parse correctly

- [ ] **Fix #6: Update LoginPage.tsx - Store credentials**
  - After login success:
    - localStorage.setItem('userId', response.userId)
    - localStorage.setItem('studentId', response.studentId)
    - localStorage.setItem('username', response.username)

- [ ] **Fix #7: Update StudentDashboard page**
  - Use useAuth() hook
  - Check auth exists before API call
  - Never pass null to API endpoint

---

## PRODUCTION DEPLOYMENT CHECKLIST

### Data Migration

```sql
-- 1. Find orphaned users (no corresponding Student records)
SELECT COUNT(*) FROM users u 
WHERE u.role = 'STUDENT' AND NOT EXISTS 
  (SELECT 1 FROM students s WHERE s.user_id = u.id);

-- 2. Auto-create Student records for orphaned STUDENT users
INSERT INTO students (user_id, college_id, enrollment_number, fees_paid)
SELECT u.id, 1, 'ENR-' || u.id, false
FROM users u
WHERE u.role = 'STUDENT' 
  AND NOT EXISTS (SELECT 1 FROM students s WHERE s.user_id = u.id);

-- 3. Verify all STUDENT users now have Student records
SELECT COUNT(*) FROM users u WHERE u.role = 'STUDENT'
HAVING COUNT(*) = (SELECT COUNT(*) FROM students);
```

### Testing Script

```bash
#!/bin/bash

# Test 1: Login
echo "Testing login..."
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"student@test.com","password":"password123"}'

# Expect: { "userId": N, "studentId": M, "username": "..." }

# Test 2: Dashboard API
STUDENT_ID=10  # Replace with actual ID from login
echo "Testing dashboard..."
curl -X GET http://localhost:8080/api/students/${STUDENT_ID}/dashboard

# Expect: 200 OK with StudentDashboardResponse
```

---

## SUMMARY TABLE

| Problem | Root Cause | Solution | Priority |
|---------|-----------|----------|----------|
| FK Constraint Error | Cascade delete not configured | Add conditional deletion logic or safe cascade | HIGH |
| studentId = null | Student record not created | Auto-create Student in register() | CRITICAL |
| Dashboard Type Error | Frontend passes "null" string | Use useAuth() hook to ensure numeric ID | HIGH |
| Wrong endpoint | N/A | Code is correct, issue is frontend | MEDIUM |

---

## NEXT STEPS

1. **Immediate** (30 min): Run SQL migration to create missing Student records
2. **Backend** (1 hour): Implement AuthService.register() fix + UserDeletionService
3. **Frontend** (45 min): Create useAuth() hook + update dashboard page
4. **Testing** (30 min): Login flow → Dashboard verification
5. **Production Deploy**: Schema changes + code release


