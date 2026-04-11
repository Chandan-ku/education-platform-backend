package com.collegenet.collegenet.service;

import com.collegenet.collegenet.entity.Semester;
import com.collegenet.collegenet.repo.SemesterRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepo semesterRepository;

    public List<Semester> getAllSemesters() {
        return semesterRepository.findAll();
    }

    public Semester createSemester(Semester semester) {
        return semesterRepository.save(semester);
    }

    public List<Semester> getSemestersByStudent(Long studentId) {
        return semesterRepository.findByStudentId(studentId);
    }
}
