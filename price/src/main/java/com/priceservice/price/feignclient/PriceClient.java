package com.priceservice.price.feignclient;

import com.priceservice.price.dto.PriceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "/api/v1/price")
public interface PriceClient {
    @GetMapping("/{id}")
    ResponseEntity<PriceResponseDTO> getPriceById(@PathVariable("id") Long id);
}
