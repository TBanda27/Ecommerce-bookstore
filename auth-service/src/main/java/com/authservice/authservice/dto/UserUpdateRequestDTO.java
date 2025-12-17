package com.authservice.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDTO(
        @Size(min = 2, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Email(message = "Email should be valid")
        String email,

        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        String confirmPassword
) {
}
