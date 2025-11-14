package com.ecommerce_books.book_service.mapper;

import com.ecommerce_books.book_service.dto.BookRequestDTO;
import com.ecommerce_books.book_service.dto.BookResponseDTO;
import com.ecommerce_books.book_service.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {
    public Book mapRequestDtoToBook(BookRequestDTO bookRequestDTO) {
        return Book.builder()
                .name(bookRequestDTO.name())
                .description(bookRequestDTO.description())
                .bookCoverImage(bookRequestDTO.bookCoverImage())
                .uniqueProductCode(bookRequestDTO.uniqueProductCode())
                .categoryId(bookRequestDTO.categoryId())
                .categoryId(bookRequestDTO.categoryId())
                .priceId(bookRequestDTO.priceId())
                .stockStatus(bookRequestDTO.stockStatus())
                .availabilityStatus(bookRequestDTO.availabilityStatus())
                .numberOfReviews(bookRequestDTO.numberOfReviews())
                .build();
    }

    public BookResponseDTO mapBookToBookResponseDTO(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getName(),
                book.getDescription(),
                book.getBookCoverImage(),
                book.getUniqueProductCode(),
                book.getCategoryId(),
                "",
                book.getPriceId(),
                "",
                "",
                "",
                book.getStockStatus(),
                book.getAvailabilityStatus(),
                book.getNumberOfReviews()
        );
    }
}
