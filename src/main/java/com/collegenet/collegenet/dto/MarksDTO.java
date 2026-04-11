package com.collegenet.collegenet.dto;

import lombok.Data;

@Data
public class MarksDTO {

    private Long id;
    private Long studentId;
    private Integer semester;
    private String subject;
    private String examType;     // INTERNAL / MID / SEMESTER
    private Integer maxMarks;
    private Integer obtainedMarks;
    private Boolean pass;
}
