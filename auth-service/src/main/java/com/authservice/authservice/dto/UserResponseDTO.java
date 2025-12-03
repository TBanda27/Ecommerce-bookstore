package com.authservice.authservice.dto;

import java.util.Set;

public record UserResponseDTO(String username, String email, Set<String> role) {
}
