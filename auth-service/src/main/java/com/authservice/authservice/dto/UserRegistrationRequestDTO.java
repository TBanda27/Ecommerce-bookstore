package com.authservice.authservice.dto;

public record UserRegistrationRequestDTO(String username,
                                         String email,
                                         String password,
                                         String confirmPassword) {
}
