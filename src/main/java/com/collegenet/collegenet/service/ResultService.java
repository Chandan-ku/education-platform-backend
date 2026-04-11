package com.collegenet.collegenet.service;

import com.collegenet.collegenet.entity.Result;
import com.collegenet.collegenet.repo.ResultRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepo resultRepository;

    public List<Result> getAllResults() {
        return resultRepository.findAll();
    }

    public Result saveResult(Result result) {
        return resultRepository.save(result);
    }

    public List<Result> getResultsByStudent(Long studentId) {
        return resultRepository.findByStudentId(studentId);
    }
}
