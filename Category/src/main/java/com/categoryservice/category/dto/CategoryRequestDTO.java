package com.categoryservice.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDTO(
        Long categoryId,

        @NotBlank(message = "Category name is required")
        @Size(min = 2, max = 255, message = "Category name must be between 2 and 255 characters")
        String categoryName
) {
}
