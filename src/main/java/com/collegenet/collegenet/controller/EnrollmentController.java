package com.collegenet.collegenet.controller;

import com.collegenet.collegenet.dto.CourseDTO;
import com.collegenet.collegenet.dto.DropCourseRequest;
import com.collegenet.collegenet.dto.EnrollmentRequest;
import com.collegenet.collegenet.dto.EnrollmentResponse;
import com.collegenet.collegenet.dto.StudentResponseDTO;
import com.collegenet.collegenet.entity.StudentCourse;
import com.collegenet.collegenet.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Enroll a student to a course
     * POST /api/enrollments/enroll
     */
    @PostMapping("/enroll")
    public ResponseEntity<?> enrollStudentToCourse(@RequestBody EnrollmentRequest request) {
        try {
            StudentCourse enrollment = enrollmentService.enrollStudentToCourse(
                    request.getStudentId(),
                    request.getCourseId()
            );

            EnrollmentResponse response = buildEnrollmentResponse(enrollment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get all courses for a student
     * GET /api/enrollments/student/{studentId}/courses
     */
    @GetMapping("/student/{studentId}/courses")
    public ResponseEntity<?> getStudentCourses(@PathVariable Long studentId) {
        try {
            List<CourseDTO> courses = enrollmentService.getStudentCourses(studentId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get all students enrolled in a course
     * GET /api/enrollments/course/{courseId}/students
     */
    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<?> getCourseStudents(@PathVariable Long courseId) {
        try {
            List<StudentResponseDTO> students = enrollmentService.getCourseStudents(courseId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Drop a course for a student
     * PUT /api/enrollments/drop
     */
    @PutMapping("/drop")
    public ResponseEntity<?> dropCourse(@RequestBody DropCourseRequest request) {
        try {
            enrollmentService.dropCourse(request.getStudentId(), request.getCourseId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Course dropped successfully");
            response.put("studentId", request.getStudentId().toString());
            response.put("courseId", request.getCourseId().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get enrollment status for a student-course pair
     * GET /api/enrollments/status/{studentId}/{courseId}
     */
    @GetMapping("/status/{studentId}/{courseId}")
    public ResponseEntity<?> getEnrollmentStatus(@PathVariable Long studentId, @PathVariable Long courseId) {
        try {
            StudentCourse.EnrollmentStatus status = enrollmentService.getEnrollmentStatus(studentId, courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("studentId", studentId);
            response.put("courseId", courseId);
            response.put("status", status);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get count of enrolled students in a course
     * GET /api/enrollments/course/{courseId}/count
     */
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<?> getEnrolledStudentCount(@PathVariable Long courseId) {
        try {
            long count = enrollmentService.getEnrolledStudentCount(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("courseId", courseId);
            response.put("enrolledStudentCount", count);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    // Helper method to build enrollment response
    private EnrollmentResponse buildEnrollmentResponse(StudentCourse enrollment) {
        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .enrollmentNumber(enrollment.getStudent().getEnrollmentNumber())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .status(enrollment.getStatus())
                .build();
    }

    // Helper method to create error response
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}

