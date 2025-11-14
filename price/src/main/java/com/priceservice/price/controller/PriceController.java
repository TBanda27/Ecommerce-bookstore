package com.priceservice.price.controller;

import com.priceservice.price.dto.PriceRequestDTO;
import com.priceservice.price.dto.PriceResponseDTO;
import com.priceservice.price.service.PriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/price")
@Slf4j
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @PostMapping
    public ResponseEntity<PriceResponseDTO> createPrice(@RequestBody PriceRequestDTO priceRequestDTO) {
        log.info("Price Controller: Request to create price: {}", priceRequestDTO);
        return new ResponseEntity<>(priceService.createPrice(priceRequestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceResponseDTO> getPriceById(@PathVariable("id") Long id) {
        log.info("Price Controller: Request to get price by id: {}", id);
        return new ResponseEntity<>(priceService.getPriceById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PriceResponseDTO> updatePrice(@PathVariable("id") Long id, @RequestBody PriceRequestDTO priceRequestDTO) {
        log.info("Price Controller: Request to update price with id: {} - {}", id, priceRequestDTO);
        return new ResponseEntity<>(priceService.updatePrice(id, priceRequestDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePrice(@PathVariable("id") Long id) {
        log.info("Price Controller: Request to delete price with id: {}", id);
        priceService.deletePrice(id);
        return ResponseEntity.noContent().build();
    }
}
