package com.ecommerce_books.book_service.feignclient;

import com.ecommerce_books.book_service.dto.PriceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRICE-SERVICE", path = "/api/v1/price")
public interface PriceClient {

    @GetMapping("/{id}")
    ResponseEntity<PriceResponseDTO> getPriceById(@PathVariable("id") Long id);
}
