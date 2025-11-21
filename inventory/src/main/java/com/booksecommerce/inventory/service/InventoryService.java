package com.booksecommerce.inventory.service;

import com.booksecommerce.inventory.dto.InventoryRequestDTO;
import com.booksecommerce.inventory.dto.InventoryResponseDTO;
import com.booksecommerce.inventory.entity.Inventory;
import com.booksecommerce.inventory.mapper.InventoryMapper;
import com.booksecommerce.inventory.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryService(InventoryRepository inventoryRepository, InventoryMapper inventoryMapper) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
    }

    public InventoryResponseDTO saveInventory(InventoryRequestDTO inventoryRequestDTO) {
        Inventory inventory = Inventory.builder()
                .bookId(inventoryRequestDTO.bookId())
                .stockQuantity(inventoryRequestDTO.stockQuantity())
                .availabilityStatus(inventoryRequestDTO.availabilityStatus())
                .build();
        Inventory savedInventory = inventoryRepository.saveAndFlush(inventory);
        return inventoryMapper.mapInventoryToInventoryResponseDTO(savedInventory);
    }

    public void deleteInventoryById(Long id){
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found with id: " + id));
        inventoryRepository.delete(inventory);
    }

    public InventoryResponseDTO getInventoryById(Long inventoryId) {
        log.info("Inventory Service: Get inventory by id: {}", inventoryId);
        Inventory inventory = inventoryRepository.getReferenceById(inventoryId);
        return inventoryMapper.mapInventoryToInventoryResponseDTO(inventory);
    }

    public Page<InventoryResponseDTO> getAllInventory(int page, int size) {
            log.info("Inventory Service: Getting All Books started - page: {}, size: {}", page, size);
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<Inventory> bookPage = inventoryRepository.findAll(pageRequest);
            return bookPage.map(inventoryMapper::mapInventoryToInventoryResponseDTO);
    }

    public InventoryResponseDTO updateInventory(Long id, InventoryRequestDTO inventoryRequestDTO) {
        log.info("Inventory Service: Updating inventory with id: {} - {}", id, inventoryRequestDTO);
        Inventory existingInventory = inventoryRepository.getReferenceById(id);

        existingInventory.setBookId(inventoryRequestDTO.bookId());
        existingInventory.setStockQuantity(inventoryRequestDTO.stockQuantity());
        existingInventory.setAvailabilityStatus(inventoryRequestDTO.availabilityStatus());

        Inventory updatedInventory = inventoryRepository.saveAndFlush(existingInventory);
        log.info("Inventory Service: Inventory updated successfully: {}", updatedInventory);
        return inventoryMapper.mapInventoryToInventoryResponseDTO(updatedInventory);
    }

}
