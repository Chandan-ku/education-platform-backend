package com.collegenet.collegenet.repo;

import com.collegenet.collegenet.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {

    /**
     * Sum of all paid amounts by student ID
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.student.id = :studentId")
    Double sumPaidAmountByStudentId(@Param("studentId") Long studentId);

    /**
     * Delete all payments for a student (for cascade delete)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Payment p WHERE p.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
}

