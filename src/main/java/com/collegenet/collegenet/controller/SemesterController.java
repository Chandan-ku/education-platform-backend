package com.collegenet.collegenet.controller;

import com.collegenet.collegenet.entity.Semester;
import com.collegenet.collegenet.service.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    public List<Semester> getAllSemesters() {
        return semesterService.getAllSemesters();
    }

    @PostMapping
    public Semester createSemester(@RequestBody Semester semester) {
        return semesterService.createSemester(semester);
    }

    @GetMapping("/student/{studentId}")
    public List<Semester> getByStudent(@PathVariable Long studentId) {
        return semesterService.getSemestersByStudent(studentId);
    }
}
