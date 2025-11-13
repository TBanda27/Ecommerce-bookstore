package com.ecommerce_books.book_service.repository;

import com.ecommerce_books.book_service.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
