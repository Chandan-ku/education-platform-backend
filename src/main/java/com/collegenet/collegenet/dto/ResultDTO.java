package com.collegenet.collegenet.dto;

import lombok.Data;

@Data
public class ResultDTO {

    private Long id;
    private Long studentId;
    private Integer semester;
    private Double sgpa;
    private Double cgpa;
    private Boolean passed;
}
