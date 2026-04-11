package com.collegenet.collegenet.dto;

import com.collegenet.collegenet.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    private Long studentId;
    private Double amount;
    private Payment.PaymentMode paymentMode;
}

