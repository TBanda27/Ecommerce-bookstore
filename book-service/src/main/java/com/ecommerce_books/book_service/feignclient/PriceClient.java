package com.ecommerce_books.book_service.feignclient;

import com.ecommerce_books.book_service.dto.PriceRequestDTO;
import com.ecommerce_books.book_service.dto.PriceResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "PRICE-SERVICE", path = "/api/v1/price")
public interface PriceClient {

    @GetMapping("/{id}")
    ResponseEntity<PriceResponseDTO> getPriceById(@PathVariable("id") Long id);

    @GetMapping("/book/{bookId}")
    ResponseEntity<PriceResponseDTO> getPriceByBookId(@PathVariable("bookId") Long bookId);

    @PostMapping
    ResponseEntity<PriceResponseDTO> savePrice(@Valid @RequestBody PriceRequestDTO priceRequestDTO);

    @DeleteMapping("/book/{bookId}")
    ResponseEntity<String> deletePriceByBookId(@PathVariable("bookId") Long bookId);
}
