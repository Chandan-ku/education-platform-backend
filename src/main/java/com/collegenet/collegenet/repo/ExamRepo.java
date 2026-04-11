package com.collegenet.collegenet.repo;

import com.collegenet.collegenet.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepo extends JpaRepository<Exam, Long> {
    List<Exam> findBySemesterId(Long semesterId);
}
