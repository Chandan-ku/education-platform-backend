package com.collegenet.collegenet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStudentDTO {

    private Long id;
    private String enrollmentNumber;
    private String studentName;
    private String collegeName;
    private Long collegeId;
    private String course;
    private Integer semester;
    private Double attendancePercentage;
    private Boolean feesPaid;
    private String email;
    private String phone;
}

