package com.ecommerce_books.book_service.mapper;

import com.ecommerce_books.book_service.dto.*;
import com.ecommerce_books.book_service.entity.Book;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BookMapper {
    public Book mapRequestDtoToBook(BookRequestDTO bookRequestDTO) {
        return Book.builder()
                .name(bookRequestDTO.name())
                .description(bookRequestDTO.description())
                .bookCoverImage(bookRequestDTO.bookCoverImage())
                .uniqueProductCode(bookRequestDTO.uniqueProductCode())
                .categoryId(bookRequestDTO.categoryId())
                .build();
    }

    public BookResponseDTO mapBookToBookResponseDTO(Book book,
                                                    CategoryResponseDTO categoryResponseDTO,
                                                    PriceResponseDTO priceResponseDTO,
                                                    InventoryResponseDTO inventoryResponseDTO) {
        return new BookResponseDTO(
                book.getId(),
                book.getName(),
                book.getDescription(),
                book.getBookCoverImage(),
                book.getUniqueProductCode(),
                book.getCategoryId(),
                categoryResponseDTO.categoryName(),
                priceResponseDTO.id(),
                priceResponseDTO.priceExclVat(),
                priceResponseDTO.priceInclVat(),
                priceResponseDTO.currency(),
                inventoryResponseDTO.stockQuantity(),
                inventoryResponseDTO.availabilityStatus(),
                0

        );
    }
}
