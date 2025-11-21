package com.ecommerce_books.book_service.service;

import com.ecommerce_books.book_service.dto.BookRequestDTO;
import com.ecommerce_books.book_service.dto.BookResponseDTO;
import com.ecommerce_books.book_service.entity.Book;
import com.ecommerce_books.book_service.mapper.BookMapper;
import com.ecommerce_books.book_service.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;

    public BookService(BookMapper bookMapper, BookRepository bookRepository) {
        this.bookMapper = bookMapper;
        this.bookRepository = bookRepository;
    }

    public BookResponseDTO saveBook(BookRequestDTO bookRequestDTO) {
        log.info("Book Service: Saving book started: {}", bookRequestDTO);
        Book book = bookMapper.mapRequestDtoToBook(bookRequestDTO);
        Book savedBook = bookRepository.saveAndFlush(book);
        log.info("Book Service: Saving book finished: {}", savedBook);
        return bookMapper.mapBookToBookResponseDTO(savedBook);
    }

    public BookResponseDTO getBookById(Long id) {
        log.info("Book Service: Request to get Book by id: {}", id);
        Book book = bookRepository.getReferenceById(id);
        return bookMapper.mapBookToBookResponseDTO(book);
    }

    public void deleteBookById( Long id) {
        log.info("Book Service: Request to delete Book by id: {}", id);
        Book book = bookRepository.getReferenceById(id);
        bookRepository.deleteById(id);
    }

    public BookResponseDTO updateBook(Long id, BookRequestDTO bookRequestDTO) {
        log.info("Book Service: Updating book with id: {} - {}", id, bookRequestDTO);
        Book existingBook = bookRepository.getReferenceById(id);

        existingBook.setName(bookRequestDTO.name());
        existingBook.setDescription(bookRequestDTO.description());
        existingBook.setBookCoverImage(bookRequestDTO.bookCoverImage());
        existingBook.setUniqueProductCode(bookRequestDTO.uniqueProductCode());
        existingBook.setCategoryId(bookRequestDTO.categoryId());
        existingBook.setPriceId(bookRequestDTO.priceId());
        existingBook.setStockStatus(bookRequestDTO.stockStatus());
        existingBook.setAvailabilityStatus(bookRequestDTO.availabilityStatus());
        existingBook.setNumberOfReviews(bookRequestDTO.numberOfReviews());

        Book updatedBook = bookRepository.saveAndFlush(existingBook);
        log.info("Book Service: Book updated successfully: {}", updatedBook);
        return bookMapper.mapBookToBookResponseDTO(updatedBook);
    }

    public Page<BookResponseDTO> getAllBooks(int page, int size) {
        log.info("Book Service: Getting All Books started - page: {}, size: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findAll(pageRequest);
        return bookPage.map(bookMapper::mapBookToBookResponseDTO);
    }
}
