package com.ecommerce_books.book_service.dto;

import jakarta.validation.constraints.*;

public record BookRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
        String name,

        @NotBlank(message = "Description is required")
        @Size(min = 2, max = 5000, message = "Description must be between 2 and 5000 characters")
        String description,

        @NotBlank(message = "Book cover image is required")
        String bookCoverImage,

        @NotBlank(message = "Unique product code is required")
        String uniqueProductCode,

        @NotNull(message = "Category ID is required")
        @Positive(message = "Category ID must be positive")
        Long categoryId,

        @NotNull(message = "Price ID is required")
        @Positive(message = "Price ID must be positive")
        Long priceId,

        @NotNull(message = "Stock status is required")
        @Min(value = 0, message = "Stock status cannot be negative")
        Integer stockStatus,

        @NotNull(message = "Availability status is required")
        Boolean availabilityStatus,

        @NotNull(message = "Number of reviews is required")
        @Min(value = 0, message = "Number of reviews cannot be negative")
        Integer numberOfReviews
) {
}
