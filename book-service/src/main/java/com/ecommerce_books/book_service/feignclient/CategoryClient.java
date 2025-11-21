package com.ecommerce_books.book_service.feignclient;

import com.ecommerce_books.book_service.dto.CategoryResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CATEGORY-SERVICE", path = "/api/v1/category")
public interface CategoryClient {

    @GetMapping("/{id}")
    ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable("id") Long id);
}
