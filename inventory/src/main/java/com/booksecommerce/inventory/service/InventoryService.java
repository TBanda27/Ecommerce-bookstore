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
        inventoryRepository.findById(id);
        inventoryRepository.deleteById(id);
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

}
