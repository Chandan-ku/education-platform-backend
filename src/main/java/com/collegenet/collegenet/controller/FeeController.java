package com.collegenet.collegenet.controller;

import com.collegenet.collegenet.dto.AssignFeeRequest;
import com.collegenet.collegenet.dto.FeeStatusResponse;
import com.collegenet.collegenet.dto.PaymentRequest;
import com.collegenet.collegenet.entity.Fee;
import com.collegenet.collegenet.entity.Payment;
import com.collegenet.collegenet.service.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    /**
     * Get all fees
     * GET /api/fees
     */
    @GetMapping
    public List<Fee> getAllFees() {
        return feeService.getAllFees();
    }

    /**
     * Assign fee to a student
     * POST /api/fees/assign
     */
    @PostMapping("/assign")
    public ResponseEntity<Fee> assignFee(@RequestBody AssignFeeRequest request) {
        Fee fee = feeService.assignFeeToStudent(
                request.getStudentId(),
                request.getTotalFee(),
                request.getAcademicYear()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(fee);
    }

    /**
     * Record a payment for a student's fee
     * POST /api/fees/pay
     */
    @PostMapping("/pay")
    public ResponseEntity<Payment> payFee(@RequestBody PaymentRequest request) {
        Payment payment = feeService.payFee(
                request.getStudentId(),
                request.getAmount(),
                request.getPaymentMode()
        );
        return ResponseEntity.status(HttpStatus.OK).body(payment);
    }

    /**
     * Get fee status for a student (totalFee, paidFee, dueFee)
     * GET /api/fees/status/{studentId}
     */
    @GetMapping("/status/{studentId}")
    public ResponseEntity<FeeStatusResponse> getFeeStatus(@PathVariable Long studentId) {
        FeeStatusResponse status = feeService.getFeeStatus(studentId);
        return ResponseEntity.ok(status);
    }

    /**
     * Get fee details for a student
     * GET /api/fees/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Fee> getStudentFee(@PathVariable Long studentId) {
        Fee fee = feeService.getFeesByStudent(studentId);
        return ResponseEntity.ok(fee);
    }

    /**
     * Create fee (legacy endpoint for backward compatibility)
     * POST /api/fees
     */
    @PostMapping
    public ResponseEntity<Fee> createFee(@RequestBody Fee fee) {
        Fee createdFee = feeService.createFee(fee);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFee);
    }
}
