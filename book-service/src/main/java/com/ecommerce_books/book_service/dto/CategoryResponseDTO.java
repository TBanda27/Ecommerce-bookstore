package com.ecommerce_books.book_service.dto;

public record CategoryResponseDTO(
        Long id,
        Long categoryId,
        String categoryName
) {
}
