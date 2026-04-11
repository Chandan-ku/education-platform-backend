package com.collegenet.collegenet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeApprovalDTO {

    private Long collegeId;
    private String collegeName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private boolean approved;
}

