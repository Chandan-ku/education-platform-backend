package com.collegenet.collegenet.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String examType; // INTERNAL / MID / SEMESTER

    private String subject;

    private Integer maxMarks;

    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;
}
