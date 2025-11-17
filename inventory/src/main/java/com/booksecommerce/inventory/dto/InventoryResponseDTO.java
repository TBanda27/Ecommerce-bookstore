package com.booksecommerce.inventory.dto;

public record InventoryResponseDTO(
        Long id,
        Long bookId,
        Integer stockQuantity,
        Boolean availabilityStatus
) {
}
