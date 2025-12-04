package com.reviews.reviews.dto;

import java.math.BigDecimal;

public record BookResponseDTO(
        Long bookId,
        String name,
        String description,
        String bookCoverImage,
        String uniqueProductCode,
        Long categoryId,
        String categoryName,
        Long priceId,
        BigDecimal priceExclVat,
        BigDecimal priceIncVat,
        String currency,
        Integer stockStatus,
        Boolean availabilityStatus,
        Integer numberOfReviews
) {
}
