package com.collegenet.collegenet.repo;

import com.collegenet.collegenet.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollegeRepo extends JpaRepository<College, Long> {

    Optional<College> findByEmail(String email);

    Optional<College> findByUserId(Long userId);
}
