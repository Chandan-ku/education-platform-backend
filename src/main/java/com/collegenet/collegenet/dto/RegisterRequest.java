package com.collegenet.collegenet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username; // optional, we will map to email or unused if not needed

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}

