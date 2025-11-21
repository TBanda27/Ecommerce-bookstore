package com.ecommerce_books.book_service.dto;

import jakarta.validation.Valid;

public record BookCompleteRequestDTO(
        @Valid BookRequestDTO bookRequestDTO,
        @Valid PriceDataDTO priceDataDTO,
        @Valid InventoryDataDTO inventoryDataDTO
        ) {
}
