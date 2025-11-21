package com.booksecommerce.inventory.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BOOK-SERVICE", path = "/api/v1/books")
public interface BookClient {

    @GetMapping("/{id}/exists")
    ResponseEntity<Boolean> checkBookExists(@PathVariable("id") Long id);
}
