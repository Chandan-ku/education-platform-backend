package com.collegenet.collegenet.repo;


import com.collegenet.collegenet.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepo extends JpaRepository<Student, Long> {

    Optional<Student> findByEnrollmentNumber(String enrollmentNumber);

    Optional<Student> findByUserId(Long userId);
}
