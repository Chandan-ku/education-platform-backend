package com.collegenet.collegenet.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentResponseDTO {

    private Long id;
    private String enrollmentNumber;
    private String course;
    private Integer semester;
    private Double attendancePercentage;
    private Boolean feesPaid;
}
