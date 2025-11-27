package com.ecommerce_books.book_service.controller;

import com.ecommerce_books.book_service.dto.BookCompleteRequestDTO;
import com.ecommerce_books.book_service.dto.BookRequestDTO;
import com.ecommerce_books.book_service.dto.BookResponseDTO;
import com.ecommerce_books.book_service.service.BookService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@Slf4j
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponseDTO> saveBook(@Valid @RequestBody BookCompleteRequestDTO bookRequestDTO) {
        log.info("Book Controller: Request to save a book: {}", bookRequestDTO);
        return new ResponseEntity<>(bookService.saveBook(bookRequestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable("id") Long id) {
        log.info("Book Controller: Request to get Book by id: {}", id);
        return new ResponseEntity<>(bookService.getBookById(id),  HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<BookResponseDTO>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId) {
        log.info("Book Controller: Request to get all books - page: {}, size: {}, categoryId: {}", page, size, categoryId);
        return new ResponseEntity<>(bookService.getAllBooks(page, size, categoryId), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook(@PathVariable("id") Long id, @Valid @RequestBody BookRequestDTO bookRequestDTO) {
        log.info("Book Controller: Request to update a book with id: {} - {}", id, bookRequestDTO);
        return new ResponseEntity<>(bookService.updateBook(id, bookRequestDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable("id") Long id) {
        log.info("Book Controller: Request to delete a book with id: {}", id);
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkBookExists(@PathVariable("id") Long id) {
        log.info("Book Controller: Request to check if book exists with id: {}", id);
        return new ResponseEntity<>(bookService.bookExists(id), HttpStatus.OK);
    }
}
