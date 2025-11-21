package com.priceservice.price.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PriceRequestDTO(
        @NotNull(message = "Book ID is required")
        @Positive(message = "Book ID must be positive")
        Long bookId,

        @NotNull(message = "Price excl VAT is required")
        @Positive(message = "Price excl VAT must be positive")
        BigDecimal priceExclVat,

        @NotNull(message = "Tax amount is required")
        @Min(value = 0, message = "Tax amount cannot be negative")
        BigDecimal taxAmount,

        @NotBlank(message = "Currency is required")
        String currency
) {
}
