package com.reviews.reviews.feignclients;

import com.reviews.reviews.dto.BookResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BOOK-SERVICE", path = "/api/v1/books")
public interface BookClient {

    @GetMapping("/{id}")
    ResponseEntity<BookResponseDTO> getBookById(@PathVariable("id") Long id);
}