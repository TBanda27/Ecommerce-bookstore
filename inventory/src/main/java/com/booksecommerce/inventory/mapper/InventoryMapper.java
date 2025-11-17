package com.booksecommerce.inventory.mapper;

import com.booksecommerce.inventory.dto.InventoryRequestDTO;
import com.booksecommerce.inventory.dto.InventoryResponseDTO;
import com.booksecommerce.inventory.entity.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {
    public Inventory mapInventoryRequestDtoToInventory(InventoryRequestDTO inventoryRequestDto) {
        return Inventory.builder()
                .availabilityStatus(inventoryRequestDto.availabilityStatus())
                .stockQuantity(inventoryRequestDto.stockQuantity())
                .bookId(inventoryRequestDto.bookId())
                .build();
    }

    public InventoryResponseDTO mapInventoryToInventoryResponseDTO(Inventory inventory) {
        return new InventoryResponseDTO(
                inventory.getId(),
                inventory.getBookId(),
                inventory.getStockQuantity(),
                inventory.getAvailabilityStatus()
        );
    }
}
