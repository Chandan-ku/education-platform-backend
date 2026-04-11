package com.collegenet.collegenet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStatusResponse {
    private Long studentId;
    private Double totalFee;
    private Double paidFee;
    private Double dueFee;
}

