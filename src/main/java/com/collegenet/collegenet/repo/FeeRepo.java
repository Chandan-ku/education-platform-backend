package com.collegenet.collegenet.repo;

import com.collegenet.collegenet.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface FeeRepo extends JpaRepository<Fee, Long> {

    /**
     * Find Fee by student ID (latest or single fee per student)
     */
    Optional<Fee> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Delete all fees for a student (for cascade delete)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Fee f WHERE f.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);
}
