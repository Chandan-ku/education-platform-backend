package com.collegenet.collegenet.repo;

import com.collegenet.collegenet.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ResultRepo extends JpaRepository<Result, Long> {
    List<Result> findByStudentId(Long studentId);

    Result findTopByStudentIdOrderByIdDesc(Long studentId);

    /**
     * Delete all results for a student (for cascade delete)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Result r WHERE r.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
}
