package com.collegenet.collegenet.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "colleges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String collegeName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String address;

    private String city;

    private String state;

    private boolean approved = false; // admin approval

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user; // college login account
}
