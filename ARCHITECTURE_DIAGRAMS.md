# Architecture Diagram - Fees & Payment Module

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         REST API CLIENT                              │
│                    (Postman / Frontend App)                          │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                    ┌────────▼────────┐
                    │  FeeController  │
                    └────────┬────────┘
                             │
                ┌────────────┼────────────┐
                │            │            │
         ┌──────▼──────┐ ┌──▼─────┐ ┌───▼─────┐
         │  POST       │ │  POST  │ │  GET    │
         │ /assign     │ │ /pay   │ │ /status │
         └──────┬──────┘ └──┬─────┘ └───┬─────┘
                │           │           │
                └───────────┼───────────┘
                            │
                    ┌───────▼────────┐
                    │  FeeService    │
                    │  (Business     │
                    │   Logic Layer) │
                    └───┬────────┬──┬┘
                        │        │  │
        ┌───────────────┘        │  └──────────────┐
        │                        │                 │
        │                        │                 │
   ┌────▼────┐            ┌─────▼──────┐     ┌───▼──────┐
   │ FeeRepo │            │PaymentRepo │     │StudentRepo
   │         │            │            │     │
   │ Methods:│            │ Methods:   │     │ Methods:
   │- find   │            │- sum paid  │     │- find by
   │  ByStudent│          │- save      │     │  id
   │- save   │            │- CRUD      │     │- find by
   │- CRUD   │            │ operations │     │  enrollment
   └────┬────┘            └──────┬─────┘     └───┬──────┘
        │                        │                │
        │                        │                │
   ┌────▼────────┐          ┌───▼────────┐  ┌───▼──────┐
   │ Database    │          │ Database   │  │ Database │
   │             │          │            │  │          │
   │ fees table  │          │ payments   │  │ students │
   │             │          │ table      │  │ table    │
   └─────────────┘          └────────────┘  └──────────┘
        │                        │
        │                        │
   ┌────▼──────────────────────▼────────────────────┐
   │         Database Query Execution               │
   │  (Transactions, ACID, Data Consistency)        │
   └───────────────────────────────────────────────┘
```

---

## Data Flow Diagram

### 1. Assign Fee Flow
```
Client Request (AssignFeeRequest)
    ↓
FeeController.assignFee()
    ↓
FeeService.assignFeeToStudent()
    │
    ├─ Validate input parameters
    ├─ Fetch Student from StudentRepo
    ├─ Create Fee object
    │
FeeRepo.save(fee)
    ↓
Database (INSERT into fees table)
    ↓
Fee object returned to Client (HTTP 201)
```

### 2. Payment Flow
```
Client Request (PaymentRequest)
    ↓
FeeController.payFee()
    ↓
FeeService.payFee()
    │
    ├─ Validate input parameters
    ├─ Fetch Student from StudentRepo
    ├─ Verify Fee exists for Student (FeeRepo)
    ├─ Get Fee Status (totalFee, paidFee, dueFee)
    ├─ Validate payment amount ≤ dueFee
    ├─ Create Payment object
    │
PaymentRepo.save(payment)
    ↓
Database (INSERT into payments table)
    ↓
Payment object returned to Client (HTTP 200)
```

### 3. Fee Status Query Flow
```
Client Request (GET /api/fees/status/{studentId})
    ↓
FeeController.getFeeStatus()
    ↓
FeeService.getFeeStatus()
    │
    ├─ Fetch Fee by studentId (FeeRepo)
    │   └─ Gets totalFee
    │
    ├─ Query sum of payments (PaymentRepo)
    │   └─ JPQL: SELECT SUM(p.amount) FROM Payment p
    │            WHERE p.student.id = :studentId
    │   └─ Gets paidFee
    │
    ├─ Calculate dueFee = totalFee - paidFee
    │
    └─ Build FeeStatusResponse
        ↓
Response (FeeStatusResponse) returned to Client (HTTP 200)
```

---

## Entity Relationship Diagram

```
┌──────────────────┐
│    Student       │
│  (Main Entity)   │
├──────────────────┤
│ - id (PK)        │
│ - enrollmentNo   │
│ - course         │
│ - semester       │
└────────┬─────────┘
         │
         │ 1:1 to many
         │
    ┌────┴──────────────────┐
    │                       │
    │ 1:1 to 1:many         │ 1:1 to 1:many
    │                       │
┌───▼────────┐      ┌──────▼───────┐
│    Fee     │      │   Payment    │
├────────────┤      ├──────────────┤
│ - id (PK)  │      │ - id (PK)    │
│ - student_ │      │ - student_id │
│   id (FK)  │      │   (FK)       │
│ - totalFee │      │ - amount     │
│ - academic │      │ - payment    │
│   year     │      │   date       │
│            │      │ - payment    │
│            │      │   mode(ENUM) │
└────────────┘      └──────────────┘
```

---

## Sequence Diagram: Complete Payment Scenario

```
Client          Controller       Service         Repository      Database
  │                  │               │               │               │
  │ POST /assign     │               │               │               │
  ├─────────────────>│               │               │               │
  │                  │ assignFee()   │               │               │
  │                  ├──────────────>│               │               │
  │                  │               │ validate()    │               │
  │                  │               │ findStudent() │               │
  │                  │               ├──────────────>│               │
  │                  │               │               │ SELECT*       │
  │                  │               │               ├──────────────>│
  │                  │               │               │<──────────────┤
  │                  │               │<──────────────┤               │
  │                  │               │ save(fee)     │               │
  │                  │               ├──────────────>│               │
  │                  │               │               │ INSERT        │
  │                  │               │               ├──────────────>│
  │                  │               │               │<──────────────┤
  │                  │               │<──────────────┤               │
  │                  │<──────────────┤               │               │
  │<─────────────────┤               │               │               │
  │ HTTP 201 + Fee  │               │               │               │
  │                  │               │               │               │
  │ POST /pay        │               │               │               │
  ├─────────────────>│               │               │               │
  │                  │ payFee()      │               │               │
  │                  ├──────────────>│               │               │
  │                  │               │ validate()    │               │
  │                  │               │ findStudent() │               │
  │                  │               ├──────────────>│               │
  │                  │               │               │ SELECT*       │
  │                  │               │               ├──────────────>│
  │                  │               │               │<──────────────┤
  │                  │               │<──────────────┤               │
  │                  │               │ findFee()     │               │
  │                  │               ├──────────────>│               │
  │                  │               │               │ SELECT*       │
  │                  │               │               ├──────────────>│
  │                  │               │               │<──────────────┤
  │                  │               │<──────────────┤               │
  │                  │               │ sumPaid()     │               │
  │                  │               ├──────────────>│               │
  │                  │               │               │ SELECT SUM()  │
  │                  │               │               ├──────────────>│
  │                  │               │               │<──────────────┤
  │                  │               │<──────────────┤               │
  │                  │               │ validateAmount│               │
  │                  │               │ save(payment) │               │
  │                  │               ├──────────────>│               │
  │                  │               │               │ INSERT        │
  │                  │               │               ├──────────────>│
  │                  │               │               │<──────────────┤
  │                  │               │<──────────────┤               │
  │                  │<──────────────┤               │               │
  │<─────────────────┤               │               │               │
  │ HTTP 200 + Pay  │               │               │               │
  │                  │               │               │               │
  │ GET /status/{id} │               │               │               │
  ├─────────────────>│               │               │               │
  │                  │ getFeeStatus()│               │               │
  │                  ├──────────────>│               │               │
  │                  │               │ findFee()     │               │
  │                  │               ├──────────────>│               │
  │                  │               │               │ SELECT*       │
  │                  │               │               ├──────────────>│
  │                  │               │               │<──────────────┤
  │                  │               │<──────────────┤               │
  │                  │               │ sumPaid()     │               │
  │                  │               ├──────────────>│               │
  │                  │               │               │ SELECT SUM()  │
  │                  │               │               ├──────────────>│
  │                  │               │               │<──────────────┤
  │                  │               │<──────────────┤               │
  │                  │               │ calculate due │               │
  │                  │               │ buildResponse │               │
  │                  │<──────────────┤               │               │
  │<─────────────────┤               │               │               │
  │ HTTP 200 +      │               │               │               │
  │ FeeStatusResp   │               │               │               │
  │                  │               │               │               │
```

---

## Class Diagram

```
┌─────────────────────────────┐
│     <<Controller>>          │
│     FeeController           │
├─────────────────────────────┤
│ - feeService: FeeService    │
├─────────────────────────────┤
│ + assignFee(request)        │
│ + payFee(request)           │
│ + getFeeStatus(studentId)   │
│ + getStudentFee(studentId)  │
│ + createFee(fee)            │
└──────────────┬──────────────┘
               │ uses
               │
┌──────────────▼──────────────┐
│     <<Service>>             │
│     FeeService              │
├─────────────────────────────┤
│ - feeRepository             │
│ - paymentRepository         │
│ - studentRepository         │
├─────────────────────────────┤
│ + assignFeeToStudent()      │
│ + payFee()                  │
│ + getFeeStatus()            │
│ + getFeesByStudent()        │
│ + createFee()               │
└──────────────┬──────────────┘
               │ uses
       ┌───────┼───────┐
       │       │       │
┌──────▼──┐ ┌──▼──────┐ │
│FeeRepo  │ │Payment  │ │
│         │ │Repo     │ │
├─────────┤ ├─────────┤ │
│ extends │ │ extends │ │
│JpaRep.. │ │JpaRep.. │ │
└─────────┘ └─────────┘ │
                        │
                   ┌────▼─────┐
                   │StudentRepo
                   ├───────────┤
                   │ extends   │
                   │JpaRep...  │
                   └───────────┘

┌──────────────────┐  ┌──────────────┐
│  <<Entity>>      │  │ <<Entity>>   │
│  Fee             │  │ Payment      │
├──────────────────┤  ├──────────────┤
│ - id             │  │ - id         │
│ - student        │◀─┤ - student    │
│ - totalFee       │  │ - amount     │
│ - academicYear   │  │ - paymentDate│
│                  │  │ - paymentMode│
└──────────────────┘  └──────────────┘

┌─────────────────────────┐
│    <<DTO>>              │
│  FeeStatusResponse      │
├─────────────────────────┤
│ - studentId             │
│ - totalFee              │
│ - paidFee               │
│ - dueFee                │
└─────────────────────────┘
```

---

## Request/Response Flow

### Request 1: Assign Fee
```
POST /api/fees/assign
Content-Type: application/json

{
  "studentId": 1,
  "totalFee": 100000.00,
  "academicYear": "2024-2025"
}

↓ (Processing)

HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 1,
  "student": {...},
  "totalFee": 100000.00,
  "academicYear": "2024-2025"
}
```

### Request 2: Make Payment
```
POST /api/fees/pay
Content-Type: application/json

{
  "studentId": 1,
  "amount": 50000.00,
  "paymentMode": "UPI"
}

↓ (Processing)

HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "student": {...},
  "amount": 50000.00,
  "paymentDate": "2026-01-16",
  "paymentMode": "UPI"
}
```

### Request 3: Get Fee Status
```
GET /api/fees/status/1

↓ (Processing)

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

## Technology Stack

| Layer | Technology | Implementation |
|-------|-----------|-----------------|
| Controller | Spring Web MVC | @RestController, @RequestMapping |
| Service | Spring Framework | @Service, @Transactional |
| Repository | Spring Data JPA | JpaRepository, JPQL |
| Entity | Jakarta Persistence | @Entity, @Table, @ManyToOne |
| DTO | Lombok | @Data, @Builder |
| Database | JPA ORM | N/A (Database agnostic) |
| Validation | Java | @NotNull, custom validation |
| Error Handling | Spring | Custom Exceptions |

---

## Transaction Flow

```
Client Request
    ↓
FeeController (Entry Point)
    ↓
@Transactional Boundary Starts
    ↓
FeeService (Business Logic)
    │
    ├─ Input Validation
    ├─ Repository Operations
    │  ├─ findById (StudentRepo)
    │  ├─ findByStudentId (FeeRepo)
    │  └─ save (FeeRepo/PaymentRepo)
    │
    └─ Calculate Results
    ↓
@Transactional Boundary Commits
    ↓
ResponseEntity (HTTP Response)
    ↓
Client Response
```

---

## Error Handling Flow

```
Client Request
    ↓
Input Validation
    ├─ Null Check → IllegalArgumentException
    ├─ Value Validation → IllegalArgumentException
    └─ Business Logic Check ↓
    
FeeService Processing
    ├─ Student Not Found → ResourceNotFoundException
    ├─ Fee Not Found → ResourceNotFoundException
    ├─ Payment Exceeds Due → IllegalArgumentException
    └─ Success ↓
    
Response Handler
    ├─ GlobalExceptionHandler catches exceptions
    ├─ Returns Error Response
    └─ HTTP Error Status (400/404/500)
    
Client Error Response
```

---

This architecture ensures:
- ✅ Clean separation of concerns
- ✅ Easy testing and maintenance
- ✅ Scalability
- ✅ Transaction safety
- ✅ Error resilience
- ✅ Performance optimization

