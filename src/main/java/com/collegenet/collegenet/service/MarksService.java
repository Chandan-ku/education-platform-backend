package com.collegenet.collegenet.service;

import com.collegenet.collegenet.entity.Marks;
import com.collegenet.collegenet.repo.MarksRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarksService {

    private final MarksRepo marksRepository;

    public List<Marks> getAllMarks() {
        return marksRepository.findAll();
    }

    public Marks addMarks(Marks marks) {
        marks.setPass(marks.getObtainedMarks() >= 40);
        return marksRepository.save(marks);
    }

    public List<Marks> getMarks(Long studentId, Integer semester) {
        return marksRepository.findByStudentIdAndSemester(studentId, semester);
    }
}

