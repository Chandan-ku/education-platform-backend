package com.collegenet.collegenet.service;

import com.collegenet.collegenet.dto.FeeStatusResponse;
import com.collegenet.collegenet.entity.Fee;
import com.collegenet.collegenet.entity.Payment;
import com.collegenet.collegenet.entity.Student;
import com.collegenet.collegenet.exception.ResourceNotFoundException;
import com.collegenet.collegenet.repo.FeeRepo;
import com.collegenet.collegenet.repo.PaymentRepo;
import com.collegenet.collegenet.repo.StudentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FeeService {

    private final FeeRepo feeRepository;
    private final PaymentRepo paymentRepository;
    private final StudentRepo studentRepository;

    /**
     * Get all fees
     */
    public List<Fee> getAllFees() {
        return feeRepository.findAll();
    }

    /**
     * Assign fee to a student for an academic year
     */
    public Fee assignFeeToStudent(Long studentId, Double totalFee, String academicYear) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        if (totalFee == null || totalFee <= 0) {
            throw new IllegalArgumentException("Total fee must be greater than zero");
        }
        if (academicYear == null || academicYear.trim().isEmpty()) {
            throw new IllegalArgumentException("Academic year is required");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        Fee fee = Fee.builder()
                .student(student)
                .totalFee(totalFee)
                .academicYear(academicYear)
                .build();

        return feeRepository.save(fee);
    }

    /**
     * Record a payment for a student
     */
    public Payment payFee(Long studentId, Double amount, Payment.PaymentMode paymentMode) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (paymentMode == null) {
            throw new IllegalArgumentException("Payment mode is required");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // Check if student has a fee assigned
        Fee fee = feeRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("No fee assigned to student ID: " + studentId));

        // Verify payment amount doesn't exceed due amount
        FeeStatusResponse status = getFeeStatus(studentId);
        if (amount > status.getDueFee()) {
            throw new IllegalArgumentException("Payment amount exceeds due fee");
        }

        Payment payment = Payment.builder()
                .student(student)
                .amount(amount)
                .paymentDate(LocalDate.now())
                .paymentMode(paymentMode)
                .build();

        return paymentRepository.save(payment);
    }

    /**
     * Get fee status for a student - returns totalFee, paidFee, and dueFee
     */
    public FeeStatusResponse getFeeStatus(Long studentId) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }

        Fee fee = feeRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("No fee assigned to student ID: " + studentId));

        Double totalFee = fee.getTotalFee() != null ? fee.getTotalFee() : 0.0;
        Double paidFee = paymentRepository.sumPaidAmountByStudentId(studentId);
        paidFee = paidFee != null ? paidFee : 0.0;
        Double dueFee = totalFee - paidFee;

        return FeeStatusResponse.builder()
                .studentId(studentId)
                .totalFee(totalFee)
                .paidFee(paidFee)
                .dueFee(Math.max(dueFee, 0.0)) // Ensure due fee is never negative
                .build();
    }

    /**
     * Get fee by student ID (existing method for backward compatibility)
     */
    public Fee getFeesByStudent(Long studentId) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        return feeRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("No fee assigned to student ID: " + studentId));
    }

    /**
     * Create fee (existing method for backward compatibility)
     */
    public Fee createFee(Fee fee) {
        if (fee == null || fee.getStudent() == null) {
            throw new IllegalArgumentException("Fee and student information is required");
        }
        if (fee.getTotalFee() == null || fee.getTotalFee() <= 0) {
            throw new IllegalArgumentException("Total fee must be greater than zero");
        }
        return feeRepository.save(fee);
    }
}
