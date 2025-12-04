package com.reviews.reviews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.NonNull;

@Schema(description = "Request object for creating or updating a review")
public record ReviewRequestDTO(
        @Schema(description = "ID of the book being reviewed", example = "1" )
        @NonNull @Positive Long bookId,

        @Schema(description = "Rating from 0 to 5", example = "4", minimum = "0", maximum = "5")
        @NotNull @Min(0) @Max(5) Integer rating,

        @Schema(description = "Review text content", example = "Great book! Highly recommend.", minLength = 3, maxLength = 500, required = true)
        @NotBlank @Size(min = 3, max = 500) String review) {
}
