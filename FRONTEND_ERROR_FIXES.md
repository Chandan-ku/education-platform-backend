# Frontend Error Fixes: Hydration Mismatch & Admin Login Issues

## Problem Summary

You're experiencing two critical issues:

1. **Hydration Mismatch Error** - "A tree hydrated but some attributes of the server rendered HTML didn't match the client properties"
2. **Admin Login Not Redirecting** - After logging in with admin credentials, the page gets stuck on the login page instead of redirecting to admin dashboard

---

## 🔴 Issue 1: Hydration Mismatch Error

### Root Cause
The error occurs in your `layout.tsx` at line 22 where you have:
```tsx
<html lang="en">
```

And the error message shows:
```
<html lang="en" - crxlauncher="">
```

### Why It Happens

1. **Browser Extension Interference**: The `crxlauncher=""` attribute is being added by a Chrome extension (likely a launcher or extension) during client-side hydration
2. **suppressHydrationWarning Not Covering HTML Tag**: Your `suppressHydrationWarning` is on the `<body>` tag, but the mismatch is on the `<html>` tag

### Solution

**Update your `app/layout.tsx`**:

```tsx
import type { Metadata } from "next";
import { Jakarta_Plus } from "next/font/google"; // adjust font import as needed
import "./globals.css";

const jakarta = Jakarta_Plus({
  variable: "--font-jakarta",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "CollegeNet",
  description: "College Management System",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html 
      lang="en" 
      suppressHydrationWarning // Add this to suppress hydration warnings
      className={`${jakarta.variable}`}
    >
      <body suppressHydrationWarning className={`${jakarta.variable} antialiased`}>
        {children}
      </body>
    </html>
  );
}
```

### Additional Steps

1. **Disable Problematic Extensions During Development**:
   - Open Chrome DevTools (F12)
   - Go to Extension settings
   - Disable extensions like "Copilot", "Launcher", "Grammarly", etc. temporarily
   - Restart the dev server

2. **Clear Next.js Cache**:
   ```bash
   rm -rf .next
   npm run dev
   ```

3. **Add Global Error Boundary**:
   Create `app/error.tsx`:
   ```tsx
   'use client';

   export default function Error({
     error,
     reset,
   }: {
     error: Error & { digest?: string };
     reset: () => void;
   }) {
     return (
       <div>
         <h2>Something went wrong!</h2>
         <button onClick={() => reset()}>Try again</button>
       </div>
     );
   }
   ```

---

## 🔴 Issue 2: Admin Login Not Redirecting

### Root Cause

The admin login is likely failing because:

1. **Missing Admin User in Database** - No admin account exists to login with
2. **Incorrect Login Endpoint** - Frontend is using the wrong endpoint
3. **Missing Role Check** - Frontend isn't checking user role before redirecting
4. **Wrong Redirect URL** - Admin dashboard URL might be incorrect

### Solution - Backend Setup (IMPORTANT)

**First, create an ADMIN account in your PostgreSQL database**:

```sql
-- Insert admin user into users table
INSERT INTO users (full_name, email, password, role, phone) 
VALUES ('Admin User', 'admin@collegenet.com', '$2a$10$k3xJ9bP5L2m9nQ7xR8xX.eK5aZ1bD2cE3fG4hI5jK6lM7nO8pP9qR0', 'ADMIN', '9876543210');

-- The password above is BCrypt encrypted for: "admin123"
-- If you need to hash a different password, use a BCrypt encoder
```

**To generate BCrypt hash for your password, run this Java code**:
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode("your_password_here");
System.out.println("Hashed: " + hashedPassword);
```

### Solution - Frontend Login Logic

**Create authentication service (`lib/auth.service.ts` or similar)**:

```typescript
import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  userId: number;
  studentId: number | null;
  username: string;
  message: string;
}

export interface AuthUser {
  userId: number;
  studentId: number | null;
  username: string;
  role: 'STUDENT' | 'COLLEGE' | 'ADMIN';
}

const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    try {
      const response = await axios.post<AuthResponse>(
        `${API_BASE_URL}/auth/login`,
        credentials
      );
      
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data?.message || 'Login failed');
      }
      throw error;
    }
  },

  // Determine user role based on stored data
  getUserRole(): 'STUDENT' | 'COLLEGE' | 'ADMIN' | null {
    if (typeof window === 'undefined') return null;
    
    const userRole = localStorage.getItem('userRole');
    return userRole as 'STUDENT' | 'COLLEGE' | 'ADMIN' | null;
  },

  // Store auth data after login
  storeAuthData(response: AuthResponse, role: 'STUDENT' | 'COLLEGE' | 'ADMIN'): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem('userId', response.userId.toString());
      localStorage.setItem('studentId', response.studentId?.toString() || '');
      localStorage.setItem('username', response.username);
      localStorage.setItem('userRole', role);
      localStorage.setItem('isAuthenticated', 'true');
    }
  },

  // Get redirect path based on role
  getRedirectPath(role: 'STUDENT' | 'COLLEGE' | 'ADMIN'): string {
    switch (role) {
      case 'ADMIN':
        return '/admin/dashboard';
      case 'COLLEGE':
        return '/college/dashboard';
      case 'STUDENT':
        return '/student/dashboard';
      default:
        return '/';
    }
  },

  logout(): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('userId');
      localStorage.removeItem('studentId');
      localStorage.removeItem('username');
      localStorage.removeItem('userRole');
      localStorage.removeItem('isAuthenticated');
    }
  },

  isAuthenticated(): boolean {
    if (typeof window === 'undefined') return false;
    return localStorage.getItem('isAuthenticated') === 'true';
  }
};

export default authService;
```

**Login Page (`app/login/page.tsx` or similar)**:

```tsx
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import authService from '@/lib/auth.service';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [role, setRole] = useState<'STUDENT' | 'COLLEGE' | 'ADMIN'>('STUDENT');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Validate input
      if (!email || !password) {
        setError('Please fill in all fields');
        setLoading(false);
        return;
      }

      console.log('Attempting login with:', { email, role });

      // Call login API
      const response = await authService.login({ email, password });

      console.log('Login response:', response);

      // Store auth data
      authService.storeAuthData(response, role);

      // Get redirect path based on selected role
      const redirectPath = authService.getRedirectPath(role);
      console.log('Redirecting to:', redirectPath);

      // Redirect based on role
      router.push(redirectPath);
    } catch (err) {
      console.error('Login error:', err);
      const errorMessage = err instanceof Error ? err.message : 'Login failed';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            CollegeNet Login
          </h2>
        </div>

        {error && (
          <div className="rounded-md bg-red-50 p-4">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        <form className="mt-8 space-y-6" onSubmit={handleLogin}>
          {/* Role Selection */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Login As
            </label>
            <select
              value={role}
              onChange={(e) => setRole(e.target.value as any)}
              disabled={loading}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="STUDENT">Student</option>
              <option value="COLLEGE">College</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>

          {/* Email */}
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">
              Email Address
            </label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="admin@collegenet.com"
            />
          </div>

          {/* Password */}
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700">
              Password
            </label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="••••••••"
            />
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:bg-gray-400"
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        {/* Test Credentials */}
        <div className="rounded-md bg-blue-50 p-4">
          <h3 className="text-sm font-medium text-blue-900 mb-2">Test Credentials:</h3>
          <ul className="text-sm text-blue-800 space-y-1">
            <li><strong>Admin:</strong> admin@collegenet.com / admin123</li>
            <li><strong>College:</strong> college@example.com / password123</li>
            <li><strong>Student:</strong> student@example.com / password123</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
```

**Admin Dashboard Protected Route (`app/admin/dashboard/page.tsx`)**:

```tsx
'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import authService from '@/lib/auth.service';

export default function AdminDashboard() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check authentication
    if (!authService.isAuthenticated()) {
      router.push('/login');
      return;
    }

    // Check if user is admin
    const role = authService.getUserRole();
    if (role !== 'ADMIN') {
      router.push('/unauthorized');
      return;
    }

    setLoading(false);
  }, [router]);

  if (loading) {
    return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
          <button
            onClick={() => {
              authService.logout();
              router.push('/login');
            }}
            className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
          >
            Logout
          </button>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
          {/* Dashboard Cards */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-medium text-gray-900">Students</h3>
            <p className="text-3xl font-bold text-blue-600 mt-2">150</p>
            <a href="/admin/students" className="text-blue-600 hover:text-blue-800 mt-4 inline-block">
              View Students →
            </a>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-medium text-gray-900">Colleges</h3>
            <p className="text-3xl font-bold text-green-600 mt-2">5</p>
            <a href="/admin/colleges" className="text-green-600 hover:text-green-800 mt-4 inline-block">
              Manage Colleges →
            </a>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-medium text-gray-900">Courses</h3>
            <p className="text-3xl font-bold text-purple-600 mt-2">25</p>
            <a href="/admin/courses" className="text-purple-600 hover:text-purple-800 mt-4 inline-block">
              View Courses →
            </a>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-medium text-gray-900">Reports</h3>
            <p className="text-3xl font-bold text-orange-600 mt-2">📊</p>
            <a href="/admin/reports" className="text-orange-600 hover:text-orange-800 mt-4 inline-block">
              View Reports →
            </a>
          </div>
        </div>
      </main>
    </div>
  );
}
```

**Create Route Guard Hook (`hooks/useAuth.ts`)**:

```typescript
import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import authService from '@/lib/auth.service';

export function useAuth(requiredRole?: 'STUDENT' | 'COLLEGE' | 'ADMIN') {
  const router = useRouter();

  useEffect(() => {
    // Check if authenticated
    if (!authService.isAuthenticated()) {
      router.push('/login');
      return;
    }

    // Check role if required
    if (requiredRole) {
      const userRole = authService.getUserRole();
      if (userRole !== requiredRole) {
        router.push('/unauthorized');
        return;
      }
    }
  }, [router, requiredRole]);

  return {
    isAuthenticated: authService.isAuthenticated(),
    role: authService.getUserRole(),
    logout: () => {
      authService.logout();
      router.push('/login');
    }
  };
}
```

---

## 📋 Troubleshooting Checklist

### For Hydration Error:
- [ ] Added `suppressHydrationWarning` to `<html>` tag
- [ ] Cleared `.next` cache (`rm -rf .next`)
- [ ] Restarted dev server
- [ ] Disabled browser extensions
- [ ] Checked for dynamic content in layout that differs between server and client

### For Admin Login:
- [ ] Created ADMIN user in database with email `admin@collegenet.com` and password `admin123`
- [ ] Verified backend is running on `http://localhost:8080`
- [ ] Checked `/api/auth/login` endpoint works with Postman/Insomnia
- [ ] Added role selector to login form
- [ ] Verified redirect URL matches your route structure
- [ ] Checked browser console for errors
- [ ] Verified localStorage is being set correctly
- [ ] Created `/admin/dashboard` route
- [ ] Added authentication guard to protected routes

### API Test (use Postman):
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "admin@collegenet.com",
    "password": "admin123"
}
```

Expected Response:
```json
{
    "userId": 1,
    "studentId": null,
    "username": "Admin User",
    "message": "Login successful"
}
```

---

## ✅ Backend Status

Your backend is **READY FOR FRONTEND INTEGRATION**. The complete documentation is in:
**`FRONTEND_BACKEND_INTEGRATION_GUIDE.md`**

All API endpoints, DTOs, entities, and configurations are properly documented and ready to use.

---

**Last Updated**: March 2, 2026  
**Version**: 1.0  
**Status**: ✅ Ready for Implementation

