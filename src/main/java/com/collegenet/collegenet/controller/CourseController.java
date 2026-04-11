package com.collegenet.collegenet.controller;

import com.collegenet.collegenet.dto.CourseDTO;
import com.collegenet.collegenet.entity.Course;
import com.collegenet.collegenet.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // ==================== Basic CRUD ====================

    /**
     * Get all courses
     * GET /api/courses
     */
    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        try {
            List<CourseDTO> courses = courseService.getAllCoursesDTO();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch courses: " + e.getMessage()));
        }
    }

    /**
     * Get course by ID
     * GET /api/courses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        try {
            CourseDTO course = courseService.getCourseDTOById(id);
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Course not found: " + e.getMessage()));
        }
    }

    /**
     * Create a new course
     * POST /api/courses
     */
    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        try {
            Course createdCourse = courseService.createCourse(course);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Failed to create course: " + e.getMessage()));
        }
    }

    /**
     * Update a course
     * PUT /api/courses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(
            @PathVariable Long id,
            @RequestBody Course courseDetails) {
        try {
            Course updatedCourse = courseService.updateCourse(id, courseDetails);
            return ResponseEntity.ok(updatedCourse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Failed to update course: " + e.getMessage()));
        }
    }

    /**
     * Delete a course
     * DELETE /api/courses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Course deleted successfully");
            response.put("courseId", id.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Failed to delete course: " + e.getMessage()));
        }
    }

    // ==================== Dynamic Course Listing ====================

    /**
     * Get courses with job guarantee
     * GET /api/courses/filter/job-guarantee?guaranteed=true
     */
    @GetMapping("/filter/job-guarantee")
    public ResponseEntity<?> getCoursesByJobGuarantee(
            @RequestParam(required = false, defaultValue = "true") Boolean guaranteed) {
        try {
            List<CourseDTO> courses = courseService.getCoursesByJobGuarantee(guaranteed);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch courses: " + e.getMessage()));
        }
    }

    /**
     * Get courses sorted by price
     * GET /api/courses/sort/price?ascending=true
     */
    @GetMapping("/sort/price")
    public ResponseEntity<?> getCoursesSortedByPrice(
            @RequestParam(required = false, defaultValue = "true") boolean ascending) {
        try {
            List<CourseDTO> courses = courseService.getCoursesSortedByPrice(ascending);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch courses: " + e.getMessage()));
        }
    }

    /**
     * Get courses by duration range
     * GET /api/courses/filter/duration?minWeeks=4&maxWeeks=12
     */
    @GetMapping("/filter/duration")
    public ResponseEntity<?> getCoursesByDurationRange(
            @RequestParam Integer minWeeks,
            @RequestParam Integer maxWeeks) {
        try {
            List<CourseDTO> courses = courseService.getCoursesByDurationRange(minWeeks, maxWeeks);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Failed to fetch courses: " + e.getMessage()));
        }
    }

    /**
     * Search courses by title/keyword
     * GET /api/courses/search?keyword=java
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchCoursesByTitle(
            @RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Keyword is required"));
            }
            List<CourseDTO> courses = courseService.searchCoursesByTitle(keyword);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to search courses: " + e.getMessage()));
        }
    }

    // ==================== Helper Methods ====================

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}