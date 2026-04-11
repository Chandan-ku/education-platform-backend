package com.collegenet.collegenet.dto;

import com.collegenet.collegenet.entity.StudentCourse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private Long enrollmentId;
    private Long studentId;
    private String enrollmentNumber;
    private Long courseId;
    private String courseTitle;
    private LocalDate enrollmentDate;
    private StudentCourse.EnrollmentStatus status;
}

