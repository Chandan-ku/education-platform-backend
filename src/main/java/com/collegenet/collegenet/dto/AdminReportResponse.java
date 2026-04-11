package com.collegenet.collegenet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReportResponse {
    private Long totalColleges;
    private Long totalStudents;
    private Long totalCourses;
    private Long totalUsers;
    private Long totalPayments;
}

