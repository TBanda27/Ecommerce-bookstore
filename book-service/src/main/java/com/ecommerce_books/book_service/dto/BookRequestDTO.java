package com.ecommerce_books.book_service.dto;

public record BookRequestDTO(
        String name,
        String description,
        String bookCoverImage,
        String uniqueProductCode,
        Long categoryId,
        Long priceId,
        Integer stockStatus,
        Boolean availabilityStatus,
        Integer numberOfReviews
) {
}
