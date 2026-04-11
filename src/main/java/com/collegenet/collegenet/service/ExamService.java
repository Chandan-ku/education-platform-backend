package com.collegenet.collegenet.service;

import com.collegenet.collegenet.entity.Exam;
import com.collegenet.collegenet.repo.ExamRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepo examRepository;

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public Exam createExam(Exam exam) {
        return examRepository.save(exam);
    }

    public List<Exam> getExamsBySemester(Long semesterId) {
        return examRepository.findBySemesterId(semesterId);
    }
}

