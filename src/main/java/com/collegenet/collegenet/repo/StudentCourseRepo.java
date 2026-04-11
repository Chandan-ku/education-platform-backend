package com.collegenet.collegenet.repo;

import com.collegenet.collegenet.entity.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface StudentCourseRepo extends JpaRepository<StudentCourse, Long> {

    List<StudentCourse> findByStudentId(Long studentId);

    List<StudentCourse> findByCourseId(Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<StudentCourse> findByStudentIdAndCourseId(Long studentId, Long courseId);

    /**
     * Delete all student course enrollments for a student (for cascade delete)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM StudentCourse sc WHERE sc.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
}

