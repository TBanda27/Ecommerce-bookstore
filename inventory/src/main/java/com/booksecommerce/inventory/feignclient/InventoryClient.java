package com.booksecommerce.inventory.feignclient;

import com.booksecommerce.inventory.dto.InventoryResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "/api/v1/inventory")
public interface InventoryClient {

    @GetMapping("/{id}")
    ResponseEntity<InventoryResponseDTO> getInventoryById(@PathVariable Long id);
}
