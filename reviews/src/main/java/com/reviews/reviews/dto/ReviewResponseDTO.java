package com.reviews.reviews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

@Schema(description = "Response object containing review details")
public record ReviewResponseDTO(
        @Schema(description = "Unique review identifier", example = "1")
        Long id,

        @Schema(description = "ID of the reviewed book", example = "1")
        Long bookId,

        @Schema(description = "Name of the reviewed book", example = "It's Only the Himalayas")
        String bookName,

        @Schema(description = "Rating given (0-5)", example = "4")
        Integer rating,

        @Schema(description = "Review text content", example = "Great book! Highly recommend.")
        String review,

        @Schema(description = "Timestamp when review was created", example = "2025-12-04T01:54:13.256+00:00")
        Date createdAt,

        @Schema(description = "Timestamp when review was last updated", example = "2025-12-04T01:54:13.256+00:00")
        Date updatedAt,

        @Schema(description = "Username of the reviewer", example = "john_updated")
        String reviewerName) {
}
