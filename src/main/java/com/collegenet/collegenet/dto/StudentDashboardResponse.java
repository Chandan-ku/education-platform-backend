package com.collegenet.collegenet.dto;

import lombok.Data;

@Data
public class StudentDashboardResponse {

    private Long studentId;
    private String studentName;
    private String collegeName;
    private String courseName;
    private String currentSemester;

    private Double cgpa;
    private String resultStatus;

    private Double totalFee;
    private Double paidFee;
    private Double dueFee;
}
