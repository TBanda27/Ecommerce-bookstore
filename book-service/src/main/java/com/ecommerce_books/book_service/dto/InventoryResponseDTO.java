package com.ecommerce_books.book_service.dto;

public record InventoryResponseDTO(
        Long id,
        Long bookId,
        Integer stockQuantity,
        Boolean availabilityStatus
) {
}
