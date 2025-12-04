package com.reviews.reviews.dto;

import java.util.Set;

public record UserResponseDTO(Long id, String username, String email, Set<String> role) {
}
