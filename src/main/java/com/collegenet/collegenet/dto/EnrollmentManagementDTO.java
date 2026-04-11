package com.collegenet.collegenet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentManagementDTO {

    private Long enrollmentId;
    private Long studentId;
    private String enrollmentNumber;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private String status; // ACTIVE, COMPLETED, DROPPED
    private String enrollmentDate;
}

