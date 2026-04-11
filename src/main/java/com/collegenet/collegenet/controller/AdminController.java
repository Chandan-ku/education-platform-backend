package com.collegenet.collegenet.controller;

import com.collegenet.collegenet.dto.AdminReportResponse;
import com.collegenet.collegenet.dto.AdminStudentDTO;
import com.collegenet.collegenet.dto.CollegeApprovalDTO;
import com.collegenet.collegenet.dto.EnrollmentManagementDTO;
import com.collegenet.collegenet.dto.StudentUpdateDTO;
import com.collegenet.collegenet.entity.College;
import com.collegenet.collegenet.entity.Course;
import com.collegenet.collegenet.entity.User;
import com.collegenet.collegenet.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== Dashboard & Reports ====================

    /**
     * Get all colleges for admin dashboard
     * GET /api/admin/colleges
     */
    @GetMapping("/colleges")
    public List<College> getAllColleges() {
        return adminService.getAllColleges();
    }

    /**
     * Get all users for admin dashboard
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }

    /**
     * Get all courses for admin dashboard
     * GET /api/admin/courses
     */
    @GetMapping("/courses")
    public List<Course> getAllCourses() {
        return adminService.getAllCourses();
    }

    /**
     * Get aggregated reports data
     * GET /api/admin/reports
     */
    @GetMapping("/reports")
    public AdminReportResponse getReports() {
        return adminService.getReports();
    }

    // ==================== Student CRUD Operations ====================

    /**
     * Get all students for admin management
     * GET /api/admin/students
     */
    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents() {
        try {
            List<AdminStudentDTO> students = adminService.getAllStudentsForAdmin();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch students: " + e.getMessage()));
        }
    }

    /**
     * Get student by ID for admin
     * GET /api/admin/students/{studentId}
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<?> getStudent(@PathVariable Long studentId) {
        try {
            AdminStudentDTO student = adminService.getStudentForAdmin(studentId);
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Student not found: " + e.getMessage()));
        }
    }

    /**
     * Update student by admin
     * PUT /api/admin/students/{studentId}
     */
    @PutMapping("/students/{studentId}")
    public ResponseEntity<?> updateStudent(
            @PathVariable Long studentId,
            @RequestBody StudentUpdateDTO updateDTO) {
        try {
            AdminStudentDTO updatedStudent = adminService.updateStudent(studentId, updateDTO);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Failed to update student: " + e.getMessage()));
        }
    }

    /**
     * Delete student by admin
     * DELETE /api/admin/students/{studentId}
     */
    @DeleteMapping("/students/{studentId}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long studentId) {
        try {
            adminService.deleteStudent(studentId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Student deleted successfully");
            response.put("studentId", studentId.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Failed to delete student: " + e.getMessage()));
        }
    }

    // ==================== College Approval Management ====================

    /**
     * Get all colleges for approval management
     * GET /api/admin/colleges/approval/all
     */
    @GetMapping("/colleges/approval/all")
    public ResponseEntity<?> getAllCollegesForApproval() {
        try {
            List<CollegeApprovalDTO> colleges = adminService.getAllCollegesForApproval();
            return ResponseEntity.ok(colleges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch colleges: " + e.getMessage()));
        }
    }

    /**
     * Get college by ID for approval management
     * GET /api/admin/colleges/approval/{collegeId}
     */
    @GetMapping("/colleges/approval/{collegeId}")
    public ResponseEntity<?> getCollegeForApproval(@PathVariable Long collegeId) {
        try {
            CollegeApprovalDTO college = adminService.getCollegeForApproval(collegeId);
            return ResponseEntity.ok(college);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("College not found: " + e.getMessage()));
        }
    }

    /**
     * Approve a college
     * PUT /api/admin/colleges/{collegeId}/approve
     */
    @PutMapping("/colleges/{collegeId}/approve")
    public ResponseEntity<?> approveCollege(@PathVariable Long collegeId) {
        try {
            CollegeApprovalDTO approvedCollege = adminService.approveCollege(collegeId);
            return ResponseEntity.ok(approvedCollege);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Failed to approve college: " + e.getMessage()));
        }
    }

    /**
     * Reject a college
     * PUT /api/admin/colleges/{collegeId}/reject
     */
    @PutMapping("/colleges/{collegeId}/reject")
    public ResponseEntity<?> rejectCollege(@PathVariable Long collegeId) {
        try {
            CollegeApprovalDTO rejectedCollege = adminService.rejectCollege(collegeId);
            return ResponseEntity.ok(rejectedCollege);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Failed to reject college: " + e.getMessage()));
        }
    }

    /**
     * Get all pending colleges
     * GET /api/admin/colleges/approval/pending
     */
    @GetMapping("/colleges/approval/pending")
    public ResponseEntity<?> getPendingColleges() {
        try {
            List<CollegeApprovalDTO> pendingColleges = adminService.getPendingColleges();
            return ResponseEntity.ok(pendingColleges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch pending colleges: " + e.getMessage()));
        }
    }

    /**
     * Get all approved colleges
     * GET /api/admin/colleges/approval/approved
     */
    @GetMapping("/colleges/approval/approved")
    public ResponseEntity<?> getApprovedColleges() {
        try {
            List<College> approvedColleges = adminService.getApprovedColleges();
            return ResponseEntity.ok(approvedColleges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch approved colleges: " + e.getMessage()));
        }
    }

    // ==================== Enrollment Management ====================

    /**
     * Get all enrollments
     * GET /api/admin/enrollments
     */
    @GetMapping("/enrollments")
    public ResponseEntity<?> getAllEnrollments() {
        try {
            List<EnrollmentManagementDTO> enrollments = adminService.getAllEnrollments();
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch enrollments: " + e.getMessage()));
        }
    }

    /**
     * Get enrollments for a specific student
     * GET /api/admin/enrollments/student/{studentId}
     */
    @GetMapping("/enrollments/student/{studentId}")
    public ResponseEntity<?> getStudentEnrollments(@PathVariable Long studentId) {
        try {
            List<EnrollmentManagementDTO> enrollments = adminService.getStudentEnrollments(studentId);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Failed to fetch enrollments: " + e.getMessage()));
        }
    }

    /**
     * Get enrollments for a specific course
     * GET /api/admin/enrollments/course/{courseId}
     */
    @GetMapping("/enrollments/course/{courseId}")
    public ResponseEntity<?> getCourseEnrollments(@PathVariable Long courseId) {
        try {
            List<EnrollmentManagementDTO> enrollments = adminService.getCourseEnrollments(courseId);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Failed to fetch enrollments: " + e.getMessage()));
        }
    }

    /**
     * Update enrollment status
     * PUT /api/admin/enrollments/{enrollmentId}/status
     */
    @PutMapping("/enrollments/{enrollmentId}/status")
    public ResponseEntity<?> updateEnrollmentStatus(
            @PathVariable Long enrollmentId,
            @RequestBody Map<String, String> request) {
        try {
            String newStatus = request.get("status");
            if (newStatus == null || newStatus.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Status is required"));
            }
            EnrollmentManagementDTO updatedEnrollment = adminService.updateEnrollmentStatus(enrollmentId, newStatus);
            return ResponseEntity.ok(updatedEnrollment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Failed to update enrollment: " + e.getMessage()));
        }
    }

    /**
     * Delete an enrollment
     * DELETE /api/admin/enrollments/{enrollmentId}
     */
    @DeleteMapping("/enrollments/{enrollmentId}")
    public ResponseEntity<?> deleteEnrollment(@PathVariable Long enrollmentId) {
        try {
            adminService.deleteEnrollment(enrollmentId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Enrollment deleted successfully");
            response.put("enrollmentId", enrollmentId.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Failed to delete enrollment: " + e.getMessage()));
        }
    }

    // ==================== Helper Methods ====================

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}

