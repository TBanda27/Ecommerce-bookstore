package com.booksecommerce.inventory.service;

import com.booksecommerce.inventory.dto.InventoryRequestDTO;
import com.booksecommerce.inventory.dto.InventoryResponseDTO;
import com.booksecommerce.inventory.entity.Inventory;
import com.booksecommerce.inventory.exceptions.BadRequestException;
import com.booksecommerce.inventory.exceptions.InventoryNotFoundException;
import com.booksecommerce.inventory.feignclient.BookClient;
import com.booksecommerce.inventory.mapper.InventoryMapper;
import com.booksecommerce.inventory.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final BookClient bookClient;

    public InventoryService(InventoryRepository inventoryRepository, InventoryMapper inventoryMapper, BookClient bookClient) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
        this.bookClient = bookClient;
    }

    public InventoryResponseDTO saveInventory(InventoryRequestDTO inventoryRequestDTO) {
        // Validate that book exists
        try {
            ResponseEntity<Boolean> response = bookClient.checkBookExists(inventoryRequestDTO.bookId());
            if (response.getBody() == null || !response.getBody()) {
                throw new BadRequestException("Cannot create inventory: Book with ID " + inventoryRequestDTO.bookId() + " does not exist");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Book with ID {} not found", inventoryRequestDTO.bookId());
            throw new BadRequestException("Cannot create inventory: Book with ID " + inventoryRequestDTO.bookId() + " does not exist");
        }

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
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found with id: " + id));

        // Check if book still exists - prevent delete if it does
        try {
            ResponseEntity<Boolean> response = bookClient.checkBookExists(inventory.getBookId());
            if (response.getBody() != null && response.getBody()) {
                log.error("Cannot delete inventory: Book with ID {} still exists", inventory.getBookId());
                throw new BadRequestException("Cannot delete inventory: Book with ID " + inventory.getBookId() + " still exists. Delete the book first.");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            // Book doesn't exist, safe to delete inventory
            log.info("Book not found, proceeding with inventory deletion");
        }

        inventoryRepository.delete(inventory);
    }

    public InventoryResponseDTO getInventoryById(Long inventoryId) {
        log.info("Inventory Service: Get inventory by id: {}", inventoryId);
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory with id: " + inventoryId + " not found"));
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

        // Validate that book exists
        try {
            ResponseEntity<Boolean> response = bookClient.checkBookExists(inventoryRequestDTO.bookId());
            if (response.getBody() == null || !response.getBody()) {
                throw new BadRequestException("Cannot update inventory: Book with ID " + inventoryRequestDTO.bookId() + " does not exist");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Book with ID {} not found", inventoryRequestDTO.bookId());
            throw new BadRequestException("Cannot update inventory: Book with ID " + inventoryRequestDTO.bookId() + " does not exist");
        }

        Inventory existingInventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory with id: " + id + " not found"));

        existingInventory.setBookId(inventoryRequestDTO.bookId());
        existingInventory.setStockQuantity(inventoryRequestDTO.stockQuantity());
        existingInventory.setAvailabilityStatus(inventoryRequestDTO.availabilityStatus());

        Inventory updatedInventory = inventoryRepository.saveAndFlush(existingInventory);
        log.info("Inventory Service: Inventory updated successfully: {}", updatedInventory);
        return inventoryMapper.mapInventoryToInventoryResponseDTO(updatedInventory);
    }

    public InventoryResponseDTO getInventoryByBookId(Long bookId) {
        log.info("Inventory Service: Getting inventory by book id: {}", bookId);
        Inventory inventory = inventoryRepository.findByBookId(bookId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory for book id: " + bookId + " not found"));
        return inventoryMapper.mapInventoryToInventoryResponseDTO(inventory);
    }

    public void deleteByBookId(Long bookId) {
        log.info("Inventory Service: Deleting inventory by book id: {}", bookId);
        Inventory inventory = inventoryRepository.findByBookId(bookId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory for book id: " + bookId + " not found"));
        inventoryRepository.delete(inventory);
    }
}
