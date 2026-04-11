package com.collegenet.collegenet.dto;

import lombok.Data;

@Data
public class FeeDTO {

    private Long id;
    private Long studentId;
    private Double amount;
    private String feeType; // COLLEGE / HOSTEL / COURSE
    private Boolean paid;
}
