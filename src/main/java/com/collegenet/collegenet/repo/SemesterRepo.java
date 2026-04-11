package com.collegenet.collegenet.repo;

import com.collegenet.collegenet.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SemesterRepo extends JpaRepository<Semester, Long> {
    List<Semester> findByStudentId(Long studentId);
}

