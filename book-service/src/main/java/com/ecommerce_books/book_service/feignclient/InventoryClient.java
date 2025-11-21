package com.ecommerce_books.book_service.feignclient;

import com.ecommerce_books.book_service.dto.InventoryRequestDTO;
import com.ecommerce_books.book_service.dto.InventoryResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "INVENTORY-SERVICE", path = "/api/v1/inventory")
public interface InventoryClient {
    @GetMapping("/{id}")
    ResponseEntity<InventoryResponseDTO> getInventoryById(@PathVariable Long id);

    @GetMapping("/book/{bookId}")
    ResponseEntity<InventoryResponseDTO> getInventoryByBookId(@PathVariable("bookId") Long bookId);

    @PostMapping
    ResponseEntity<InventoryResponseDTO> saveInventory(@Valid @RequestBody InventoryRequestDTO inventoryRequestDTO);

    @DeleteMapping("/book/{bookId}")
    ResponseEntity<String> deleteInventoryByBookId(@PathVariable("bookId") Long bookId);

}
