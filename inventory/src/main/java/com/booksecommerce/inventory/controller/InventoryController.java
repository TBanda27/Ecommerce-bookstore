package com.booksecommerce.inventory.controller;

import com.booksecommerce.inventory.dto.InventoryRequestDTO;
import com.booksecommerce.inventory.dto.InventoryResponseDTO;
import com.booksecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@Slf4j
public class InventoryController {
    
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> saveInventory(@Valid @RequestBody InventoryRequestDTO inventoryRequestDTO){
        log.info("Inventory Controller: saveInventory: {}", inventoryRequestDTO);
        return new ResponseEntity<>(inventoryService.saveInventory(inventoryRequestDTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInventoryById(@PathVariable Long id){
        log.info("Inventory Controller: deleteInventoryById: {}", id);
        inventoryService.deleteInventoryById(id);
        return  new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping("/book/{bookId}")
    ResponseEntity<InventoryResponseDTO> getInventoryByBookId(@PathVariable("bookId") Long bookId){
        log.info("Inventory Controller: getInventoryByBookId: {}", bookId);
        return new ResponseEntity<>(inventoryService.getInventoryByBookId(bookId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<InventoryResponseDTO>> getAllInventory(
            @RequestParam(defaultValue = "0") int page,
             @RequestParam(defaultValue = "10") int size){
        log.info("Inventory Controller getAllInventory: page {} of size {}", page, size);
        return new ResponseEntity<>(inventoryService.getAllInventory(page, size), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> getInventoryById(@PathVariable Long id){
        log.info("Inventory Controller getInventoryById: id {}", id);
        return new ResponseEntity<>(inventoryService.getInventoryById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> updateInventory(@PathVariable("id") Long id, @Valid @RequestBody InventoryRequestDTO inventoryRequestDTO){
        log.info("Inventory Controller: Request to update inventory with id: {} - {}", id, inventoryRequestDTO);
        return new ResponseEntity<>(inventoryService.updateInventory(id, inventoryRequestDTO), HttpStatus.OK);
    }

    @DeleteMapping("/book/{bookId}")
    ResponseEntity<String> deletePriceByBookId(@PathVariable("bookId") Long bookId){
        log.info("Inventory Controller: Request to delete book by book id: {}", bookId);
        inventoryService.deleteByBookId(bookId);
        return ResponseEntity.noContent().build();
    }
}
