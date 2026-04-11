package com.collegenet.collegenet.controller;

import com.collegenet.collegenet.entity.Result;
import com.collegenet.collegenet.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @GetMapping
    public List<Result> getAllResults() {
        return resultService.getAllResults();
    }

    @PostMapping
    public Result uploadResult(@RequestBody Result result) {
        return resultService.saveResult(result);
    }

    @GetMapping("/student/{studentId}")
    public List<Result> getStudentResults(@PathVariable Long studentId) {
        return resultService.getResultsByStudent(studentId);
    }
}
