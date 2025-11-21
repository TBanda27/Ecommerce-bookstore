package com.ecommerce_books.book_service.feignclient;

import com.ecommerce_books.book_service.dto.BookResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "/api/v1/books")
public interface BookClient {
    @GetMapping("/{id}")
    ResponseEntity<BookResponseDTO> getBookById(@PathVariable("id") Long id);
}
