package com.collegenet.collegenet.controller;
import com.collegenet.collegenet.entity.Exam;
import com.collegenet.collegenet.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public List<Exam> getAllExams() {
        return examService.getAllExams();
    }

    @PostMapping
    public Exam createExam(@RequestBody Exam exam) {
        return examService.createExam(exam);
    }

    @GetMapping("/semester/{semesterId}")
    public List<Exam> getBySemester(@PathVariable Long semesterId) {
        return examService.getExamsBySemester(semesterId);
    }
}
