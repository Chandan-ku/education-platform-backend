package com.collegenet.collegenet.dto;

import lombok.Data;

@Data
public class CourseDTO {

    private Long id;
    private String title;
    private String description;
    private Double price;
    private Boolean jobGuaranteed;
    private Integer durationInWeeks;
}
