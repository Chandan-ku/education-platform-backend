# Quick Reference Guide - Fees & Payment APIs

## 📱 API Endpoints Quick Reference

### 1️⃣ Assign Fee to Student
```http
POST /api/fees/assign HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "studentId": 1,
  "totalFee": 100000.00,
  "academicYear": "2024-2025"
}
```
**Response:** `HTTP 201 Created` + Fee object

---

### 2️⃣ Record Payment
```http
POST /api/fees/pay HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "studentId": 1,
  "amount": 50000.00,
  "paymentMode": "UPI"
}
```
**Response:** `HTTP 200 OK` + Payment object

**Payment Modes Available:**
- `UPI` - Unified Payments Interface
- `CASH` - Cash payment
- `CARD` - Credit/Debit Card

---

### 3️⃣ Get Fee Status
```http
GET /api/fees/status/1 HTTP/1.1
Host: localhost:8080
```

**Response:** `HTTP 200 OK`
```json
{
  "studentId": 1,
  "totalFee": 100000.00,
  "paidFee": 50000.00,
  "dueFee": 50000.00
}
```

---

## 🔑 Key Concepts

### Fee Model
- Each student has **one Fee record per academic year**
- Fee contains total amount assigned
- Multiple Payments can be made against one Fee

### Payment Model
- Records individual payment transactions
- Cannot exceed the due amount
- Stores payment mode for reporting

### Fee Status Calculation
```
totalFee = Amount assigned to student
paidFee = Sum of all payments
dueFee = totalFee - paidFee
```

---

## ✅ Validation Rules

### Assigning Fee
- ✓ studentId must be > 0
- ✓ totalFee must be > 0
- ✓ academicYear cannot be empty
- ✓ Student must exist in system

### Making Payment
- ✓ studentId must be > 0
- ✓ amount must be > 0
- ✓ paymentMode must be valid (UPI/CASH/CARD)
- ✓ Student must exist
- ✓ Fee must be assigned first
- ✓ Payment amount cannot exceed due fee

### Getting Status
- ✓ studentId must be > 0
- ✓ Student must exist
- ✓ Fee must be assigned

---

## 🚨 Error Responses

### 400 Bad Request
**Cause:** Invalid input parameters
```json
{
  "error": "Invalid student ID",
  "status": 400,
  "timestamp": "2026-01-16T10:30:00"
}
```

### 404 Not Found
**Cause:** Resource doesn't exist
```json
{
  "error": "Student not found with ID: 99",
  "status": 404,
  "timestamp": "2026-01-16T10:30:00"
}
```

### 409 Conflict
**Cause:** Business logic violation
```json
{
  "error": "Payment amount exceeds due fee",
  "status": 409,
  "timestamp": "2026-01-16T10:30:00"
}
```

---

## 💾 Database Tables

### fees
```sql
SELECT * FROM fees;
```
| id | student_id | total_fee | academic_year |
|--|--|--|--|
| 1 | 1 | 100000.00 | 2024-2025 |
| 2 | 2 | 85000.00 | 2024-2025 |

### payments
```sql
SELECT * FROM payments;
```
| id | student_id | amount | payment_date | payment_mode |
|--|--|--|--|--|
| 1 | 1 | 50000.00 | 2026-01-15 | UPI |
| 2 | 1 | 50000.00 | 2026-01-16 | CARD |

---

## 🔍 Query Examples

### Get total paid amount for a student
```sql
SELECT SUM(amount) FROM payments 
WHERE student_id = 1;
```

### Get all payments for a student
```sql
SELECT * FROM payments 
WHERE student_id = 1 
ORDER BY payment_date DESC;
```

### Get students with pending fees
```sql
SELECT s.id, s.enrollment_number, 
       f.total_fee, 
       SUM(p.amount) as paid,
       f.total_fee - COALESCE(SUM(p.amount), 0) as due
FROM students s
LEFT JOIN fees f ON s.id = f.student_id
LEFT JOIN payments p ON s.id = p.student_id
GROUP BY s.id
HAVING due > 0;
```

---

## 🧪 CURL Commands for Testing

### Assign Fee
```bash
curl -X POST http://localhost:8080/api/fees/assign \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "totalFee": 100000,
    "academicYear": "2024-2025"
  }'
```

### Make Payment
```bash
curl -X POST http://localhost:8080/api/fees/pay \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "amount": 25000,
    "paymentMode": "UPI"
  }'
```

### Get Status
```bash
curl -X GET http://localhost:8080/api/fees/status/1 \
  -H "Content-Type: application/json"
```

### Get Student Fee
```bash
curl -X GET http://localhost:8080/api/fees/student/1 \
  -H "Content-Type: application/json"
```

---

## 📊 Workflow Example

### Complete Payment Process

```
Step 1: Create Student (using StudentService)
   → Student ID: 1

Step 2: Assign Fee
   POST /api/fees/assign
   {
     "studentId": 1,
     "totalFee": 100000,
     "academicYear": "2024-2025"
   }
   → Fee ID: 1

Step 3: Check Initial Status
   GET /api/fees/status/1
   → {
       "studentId": 1,
       "totalFee": 100000,
       "paidFee": 0,
       "dueFee": 100000
     }

Step 4: Make First Payment (₹30,000)
   POST /api/fees/pay
   {
     "studentId": 1,
     "amount": 30000,
     "paymentMode": "UPI"
   }
   → Payment ID: 1

Step 5: Check Status After First Payment
   GET /api/fees/status/1
   → {
       "studentId": 1,
       "totalFee": 100000,
       "paidFee": 30000,
       "dueFee": 70000
     }

Step 6: Make Second Payment (₹40,000)
   POST /api/fees/pay
   {
     "studentId": 1,
     "amount": 40000,
     "paymentMode": "CARD"
   }
   → Payment ID: 2

Step 7: Check Status After Second Payment
   GET /api/fees/status/1
   → {
       "studentId": 1,
       "totalFee": 100000,
       "paidFee": 70000,
       "dueFee": 30000
     }

Step 8: Make Final Payment (₹30,000)
   POST /api/fees/pay
   {
     "studentId": 1,
     "amount": 30000,
     "paymentMode": "CASH"
   }
   → Payment ID: 3

Step 9: Check Final Status
   GET /api/fees/status/1
   → {
       "studentId": 1,
       "totalFee": 100000,
       "paidFee": 100000,
       "dueFee": 0
     }
   ✅ Fee Paid in Full!
```

---

## 📝 Request/Response Examples

### Full Request/Response Cycle

#### Request 1: Assign ₹100,000 Fee
```http
POST /api/fees/assign HTTP/1.1
Content-Type: application/json

{
  "studentId": 1,
  "totalFee": 100000.00,
  "academicYear": "2024-2025"
}
```

```http
HTTP/1.1 201 Created
Content-Type: application/json
Location: /api/fees/1

{
  "id": 1,
  "student": {
    "id": 1,
    "enrollmentNumber": "STU001",
    "course": "B.Tech",
    "semester": 4
  },
  "totalFee": 100000.00,
  "academicYear": "2024-2025"
}
```

#### Request 2: Make ₹50,000 Payment
```http
POST /api/fees/pay HTTP/1.1
Content-Type: application/json

{
  "studentId": 1,
  "amount": 50000.00,
  "paymentMode": "UPI"
}
```

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "student": {
    "id": 1,
    "enrollmentNumber": "STU001"
  },
  "amount": 50000.00,
  "paymentDate": "2026-01-16",
  "paymentMode": "UPI"
}
```

#### Request 3: Get Fee Status
```http
GET /api/fees/status/1 HTTP/1.1
```

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "studentId": 1,
  "totalFee": 100000.00,
  "paidFee": 50000.00,
  "dueFee": 50000.00
}
```

---

## 🔐 HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET, POST payment |
| 201 | Created | Successful POST (fees assigned) |
| 400 | Bad Request | Invalid input parameters |
| 404 | Not Found | Student/Fee not found |
| 409 | Conflict | Payment exceeds due amount |
| 500 | Server Error | Database or server issues |

---

## 📚 Service Layer Methods

### FeeService Public API

```java
// Assign fee
public Fee assignFeeToStudent(
  Long studentId, 
  Double totalFee, 
  String academicYear
)

// Record payment
public Payment payFee(
  Long studentId, 
  Double amount, 
  Payment.PaymentMode paymentMode
)

// Get fee status
public FeeStatusResponse getFeeStatus(
  Long studentId
)

// Get fee details
public Fee getFeesByStudent(
  Long studentId
)

// Create fee (direct)
public Fee createFee(
  Fee fee
)
```

---

## 🎯 Common Use Cases

### Use Case 1: New Student Fee Assignment
```
1. Admin assigns ₹100,000 fee to Student #1
2. System creates Fee record
3. Student can now make payments
```

### Use Case 2: Partial Payment
```
1. Student makes ₹30,000 payment (UPI)
2. System records payment
3. Due fee is now ₹70,000
```

### Use Case 3: Multiple Installments
```
1. Student pays ₹25,000 (CARD)
2. Student pays ₹25,000 (UPI)
3. Student pays ₹25,000 (CASH)
4. Student pays ₹25,000 (UPI)
5. Fee fully paid!
```

### Use Case 4: Fee Status Report
```
1. Admin queries fee status for Student #1
2. System returns: ₹100,000 total, ₹75,000 paid, ₹25,000 due
3. Admin generates report
```

---

## 📖 Documentation Links

- **Full Implementation Guide**: FEES_IMPLEMENTATION_GUIDE.md
- **Architecture Diagrams**: ARCHITECTURE_DIAGRAMS.md
- **Verification Report**: IMPLEMENTATION_VERIFICATION_REPORT.md

---

## 💡 Tips & Tricks

1. **Always check fee status before making payment** to avoid overpayment
2. **Payment mode is important** for audit trail and reconciliation
3. **Use academicYear as string** (e.g., "2024-2025")
4. **Multiple payments can be made** in installments
5. **StudentId must exist** before assigning fee
6. **Due fee is calculated in real-time** based on total and paid amounts

---

**Happy fee management! 🎓**

