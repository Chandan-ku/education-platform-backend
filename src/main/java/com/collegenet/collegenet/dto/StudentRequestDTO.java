package com.collegenet.collegenet.dto;

import lombok.Data;

@Data
public class StudentRequestDTO {

    private Long userId;
    private Long collegeId;
    private String enrollmentNumber;
    private String course;
    private Integer semester;
}
