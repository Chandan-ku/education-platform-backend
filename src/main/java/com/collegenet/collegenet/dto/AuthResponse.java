package com.collegenet.collegenet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private Long studentId;
    private String username;
    private String message;
}

