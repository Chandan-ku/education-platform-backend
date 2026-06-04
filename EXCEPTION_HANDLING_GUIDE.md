# CollegeNet Backend - Exception Handling Guide

## 📋 Overview

This document explains how exceptions are handled in the CollegeNet backend, specifically addressing the **"Email is already in use"** issue and how it's properly managed through custom exceptions and global exception handlers.

---

## 🔴 Issue: "Email is already in use" Exception

### Problem
When registering a user with an email that already exists, the system was incorrectly returning:
```json
{
  "timestamp": "2026-06-04T20:45:11.5639463",
  "status": 401,
  "error": "Unauthorized",
  "message": "Email is already in use",
  "path": "/api/auth/register"
}
```

**Issue**: HTTP Status **401 (Unauthorized)** is incorrect. This should be **409 (Conflict)** because:
- **401 Unauthorized** = Authentication failure (wrong credentials)
- **409 Conflict** = Resource conflict (duplicate email)

---

## ✅ Solution: Custom Exception Handling

### 1. **New Custom Exception Class**
**File**: `src/main/java/com/collegenet/collegenet/exception/DuplicateEmailException.java`

```java
package com.collegenet.collegenet.exception;

/**
 * Exception thrown when attempting to register with an email that is already in use.
 * This is a client error (400/409) not an authentication error (401).
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**Purpose**: 
- Represents a business logic violation (duplicate email)
- Distinct from authentication errors
- Allows proper HTTP status code mapping

---

### 2. **Updated GlobalExceptionHandler**
**File**: `src/main/java/com/collegenet/collegenet/exception/GlobalExceptionHandler.java`

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    // ... existing handlers ...

    /**
     * Handle DuplicateEmailException
     * Returns 409 Conflict when email already exists
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex, 
            WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())  // 409
                .error("Conflict")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle AuthenticationException
     * Returns 401 Unauthorized for invalid credentials
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, 
            WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())  // 401
                .error("Unauthorized")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    // ... other handlers ...
}
```

---

### 3. **Updated AuthService**
**File**: `src/main/java/com/collegenet/collegenet/service/AuthService.java`

```java
@Service
public class AuthService {
    
    private final UserRepo userRepo;
    private final StudentRepo studentRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Register a new user
     * @param request RegisterRequest with email and password
     * @return AuthResponse with user details
     * @throws DuplicateEmailException if email already exists
     * @throws IllegalArgumentException if validation fails
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if email exists - throws 409 Conflict
        if (userRepo.findByEmail(request.getEmail()) != null) {
            throw new DuplicateEmailException("Email is already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user.setFullName(request.getUsername());

        User saved = userRepo.save(user);
        logger.info("User created: id={}, email={}, role={}", 
                    saved.getId(), saved.getEmail(), saved.getRole());

        return new AuthResponse(saved.getId(), null, saved.getEmail(), 
                                "User registered successfully");
    }

    /**
     * Login user
     * @param request LoginRequest with email/username and password
     * @return AuthResponse with user details
     * @throws AuthenticationException if credentials invalid - throws 401 Unauthorized
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepo.findByEmail(request.getUsernameOrEmail());
        if (user == null) {
            throw new AuthenticationException("Invalid email or password");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        return new AuthResponse(user.getId(), null, user.getEmail(), "Login successful");
    }
}
```

---

## 📊 Exception Hierarchy

```
RuntimeException
├── AuthenticationException → HTTP 401 (Unauthorized)
│   ├── Invalid email
│   ├── Invalid password
│   └── Invalid credentials
│
├── DuplicateEmailException → HTTP 409 (Conflict)
│   └── Email already in use
│
├── ResourceNotFoundException → HTTP 404 (Not Found)
│   ├── Student not found
│   ├── College not found
│   └── Course not found
│
├── IllegalArgumentException → HTTP 400 (Bad Request)
│   ├── Invalid input
│   ├── Missing required fields
│   └── Validation errors
│
└── Exception → HTTP 500 (Internal Server Error)
    └── Unexpected errors
```

---

## 📡 HTTP Status Code Mapping

| Exception | HTTP Status | Use Case |
|-----------|------------|----------|
| `DuplicateEmailException` | 409 Conflict | Email already registered |
| `AuthenticationException` | 401 Unauthorized | Invalid credentials |
| `ResourceNotFoundException` | 404 Not Found | Entity not found |
| `IllegalArgumentException` | 400 Bad Request | Invalid input/validation |
| Generic `Exception` | 500 Internal Server Error | Unexpected server error |

---

## 🔧 How to Handle These Exceptions in Frontend

### 1. **Registration - Duplicate Email (409 Conflict)**

```javascript
// Frontend (React/Next.js)
try {
    const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, username })
    });

    const data = await response.json();

    if (response.status === 409) {
        // Email already exists
        showError("This email is already registered. Please use a different email.");
    } else if (response.ok) {
        // Registration successful
        navigateTo('/login');
    }
} catch (error) {
    showError("Registration failed. Please try again.");
}
```

### 2. **Login - Invalid Credentials (401 Unauthorized)**

```javascript
try {
    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usernameOrEmail: email, password })
    });

    const data = await response.json();

    if (response.status === 401) {
        // Invalid credentials
        showError("Invalid email or password.");
    } else if (response.ok) {
        // Login successful
        localStorage.setItem('token', data.token);
        navigateTo('/dashboard');
    }
} catch (error) {
    showError("Login failed. Please try again.");
}
```

---

## 📝 Files Modified/Created

### Created Files:
1. ✅ `src/main/java/com/collegenet/collegenet/exception/DuplicateEmailException.java`

### Modified Files:
1. ✅ `src/main/java/com/collegenet/collegenet/exception/GlobalExceptionHandler.java`
   - Added `@ExceptionHandler(DuplicateEmailException.class)` method
   - Returns HTTP 409 status code

2. ✅ `src/main/java/com/collegenet/collegenet/service/AuthService.java`
   - Changed import: Added `DuplicateEmailException`
   - Changed exception: `AuthenticationException` → `DuplicateEmailException` in register method

---

## 🚀 Error Response Format

All exceptions follow the `ErrorResponse` DTO:

```java
@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;    // When error occurred
    private Integer status;             // HTTP status code
    private String error;               // Error type name
    private String message;             // Error message
    private String path;                // Request path
}
```

### Example Responses:

**Register - Duplicate Email (409)**:
```json
{
  "timestamp": "2026-06-04T20:45:11.000+05:30",
  "status": 409,
  "error": "Conflict",
  "message": "Email is already in use",
  "path": "/api/auth/register"
}
```

**Login - Invalid Credentials (401)**:
```json
{
  "timestamp": "2026-06-04T20:46:22.000+05:30",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/auth/login"
}
```

**Bad Request (400)**:
```json
{
  "timestamp": "2026-06-04T20:47:33.000+05:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input",
  "path": "/api/auth/register"
}
```

---

## ✨ Best Practices

### 1. **Use Specific Exceptions**
```java
// ✅ Good - Specific exception with appropriate HTTP status
throw new DuplicateEmailException("Email is already in use");

// ❌ Bad - Generic exception with wrong HTTP status
throw new AuthenticationException("Email is already in use");
```

### 2. **Clear Error Messages**
```java
// ✅ Good - User-friendly message
"Email is already in use"

// ❌ Bad - Technical jargon
"Integrity constraint violation: unique constraint"
```

### 3. **Proper Exception Handling in Services**
```java
// ✅ Good - Throw business exception
if (userRepo.findByEmail(email) != null) {
    throw new DuplicateEmailException("Email is already in use");
}

// ❌ Bad - Catch and suppress
try {
    userRepo.save(user);
} catch (ConstraintViolationException e) {
    // Suppress silently
}
```

### 4. **Log Important Events**
```java
logger.info("User created: id={}, email={}, role={}", 
            user.getId(), user.getEmail(), user.getRole());
logger.warn("Login attempt failed for email: {}", email);
logger.error("Database error while registering user", exception);
```

---

## 🧪 Testing Exception Handling

### Test Case 1: Duplicate Email Registration

```bash
# First registration - Success
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "username": "testuser"
  }'

# Second registration with same email - 409 Conflict
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password456",
    "username": "testuser2"
  }'
# Response: 409 Conflict - "Email is already in use"
```

### Test Case 2: Invalid Login

```bash
# Invalid credentials - 401 Unauthorized
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "test@example.com",
    "password": "wrongpassword"
  }'
# Response: 401 Unauthorized - "Invalid email or password"
```

---

## 📚 Related Documentation

- **Backend Complete Documentation**: `BACKEND_COMPLETE_DOCUMENTATION.md`
- **Frontend Integration Guide**: `FRONTEND_BACKEND_INTEGRATION_GUIDE.md`
- **API Mappings**: `COMPLETE_API_MAPPINGS.md`
- **Error Fixes**: `FRONTEND_ERROR_FIXES.md`

---

## 🎯 Summary

| Aspect | Details |
|--------|---------|
| **Issue** | "Email is already in use" returned 401 instead of 409 |
| **Root Cause** | Wrong exception type used in AuthService |
| **Solution** | Created `DuplicateEmailException` + proper handler |
| **Files Created** | 1 new exception class |
| **Files Modified** | 2 files (GlobalExceptionHandler + AuthService) |
| **HTTP Status** | Now correctly returns 409 Conflict |
| **Frontend Impact** | Can now properly distinguish 409 from 401 |

**Status**: ✅ FIXED AND TESTED


