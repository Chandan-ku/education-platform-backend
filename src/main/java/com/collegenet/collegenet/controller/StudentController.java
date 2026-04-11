package com.collegenet.collegenet.controller;

import com.collegenet.collegenet.dto.StudentDashboardResponse;
import com.collegenet.collegenet.dto.StudentRequestDTO;
import com.collegenet.collegenet.dto.StudentResponseDTO;
import com.collegenet.collegenet.entity.Student;
import com.collegenet.collegenet.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;


    @PostMapping
    public StudentResponseDTO createStudent(@RequestBody StudentRequestDTO dto) {
        return studentService.createStudent(dto);
    }


    @GetMapping("/{id}")
    public Student getStudent(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    @GetMapping
    public List<Student> getAllStudents()  {
        return studentService.getAllStudents();
    }

    @GetMapping("/enrollment/{enrollmentNumber}")
    public Student getByEnrollment(@PathVariable String enrollmentNumber) {
        return studentService.getByEnrollmentNumber(enrollmentNumber);
    }
    @GetMapping("/{studentId}/dashboard")
    public ResponseEntity<StudentDashboardResponse> getDashboard(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                studentService.getStudentDashboard(studentId)
        );
    }

}
