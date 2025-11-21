package com.booksecommerce.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryRequestDTO(
        @NotNull(message = "Book ID is required")
        @Positive(message = "Book ID must be positive")
        Long bookId,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        @NotNull(message = "Availability status is required")
        Boolean availabilityStatus
) {
}
