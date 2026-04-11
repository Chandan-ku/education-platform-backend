package com.collegenet.collegenet.repo;

import com.collegenet.collegenet.entity.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MarksRepo extends JpaRepository<Marks, Long> {

    List<Marks> findByStudentIdAndSemester(Long studentId, Integer semester);

    /**
     * Delete all marks for a student (for cascade delete)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Marks m WHERE m.studentId = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
}
