package com.categoryservice.category.feignclient;

import com.categoryservice.category.dto.CategoryResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "/api/v1/category")
public interface CategoryClient {

    @GetMapping("/{id}")
    ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable("id") Long id);
}
