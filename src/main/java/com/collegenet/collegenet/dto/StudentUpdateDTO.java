package com.collegenet.collegenet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentUpdateDTO {

    private String enrollmentNumber;
    private String course;
    private Integer semester;
    private Double attendancePercentage;
    private Boolean feesPaid;
}

