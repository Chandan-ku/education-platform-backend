package com.collegenet.collegenet.entity;

import com.collegenet.collegenet.entity.College;
import com.collegenet.collegenet.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to User (login account)
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Link to College
    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false, unique = true)
    private String enrollmentNumber;

    private String course;
    private Integer semester;
    private Double attendancePercentage;

    private Boolean feesPaid;
}
