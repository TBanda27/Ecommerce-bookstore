package com.ecommerce_books.book_service.dto;

public record BookResponseDTO(
        Long bookId,
        String name,
        String description,
        String bookCoverImage,
        String uniqueProductCode,
        Long categoryId,
        String categoryName,
        Long priceId,
        String priceExclVat,
        String priceIncVat,
        String currency,
        Integer stockStatus,
        Boolean availabilityStatus,
        Integer numberOfReviews
) {
}
