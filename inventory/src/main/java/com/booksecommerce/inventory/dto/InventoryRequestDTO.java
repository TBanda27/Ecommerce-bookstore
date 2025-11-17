package com.booksecommerce.inventory.dto;

public record InventoryRequestDTO(Long bookId,
                                  Integer stockQuantity,
                                  Boolean availabilityStatus
) {
}
