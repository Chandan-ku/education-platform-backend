package com.collegenet.collegenet.entity;

import com.collegenet.collegenet.entity.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer semesterNumber; // 1,2,3,4,5,6,7,8

    private String academicYear; // 2024-2025

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
}

