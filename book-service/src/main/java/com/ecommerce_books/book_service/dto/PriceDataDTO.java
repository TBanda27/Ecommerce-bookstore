package com.ecommerce_books.book_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PriceDataDTO(
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
