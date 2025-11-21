package com.ecommerce_books.book_service.repository;

import com.ecommerce_books.book_service.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);
}
