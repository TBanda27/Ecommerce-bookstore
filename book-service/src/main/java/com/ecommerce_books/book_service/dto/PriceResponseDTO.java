package com.ecommerce_books.book_service.dto;

import java.math.BigDecimal;

public record PriceResponseDTO(
        Long id,
        Long bookId,
        BigDecimal priceExclVat,
        BigDecimal priceInclVat,
        BigDecimal taxAmount,
        String currency
) {
}
