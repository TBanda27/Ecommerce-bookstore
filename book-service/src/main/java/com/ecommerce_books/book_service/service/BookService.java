package com.ecommerce_books.book_service.service;

import com.ecommerce_books.book_service.dto.*;
import com.ecommerce_books.book_service.entity.Book;
import com.ecommerce_books.book_service.feignclient.CategoryClient;
import com.ecommerce_books.book_service.feignclient.InventoryClient;
import com.ecommerce_books.book_service.feignclient.PriceClient;
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
    private final CategoryClient categoryClient;
    private final PriceClient priceClient;
    private final InventoryClient inventoryClient;

    public BookService(BookMapper bookMapper, BookRepository bookRepository, CategoryClient categoryClient, PriceClient priceClient, InventoryClient inventoryClient) {
        this.bookMapper = bookMapper;
        this.bookRepository = bookRepository;
        this.categoryClient = categoryClient;
        this.priceClient = priceClient;
        this.inventoryClient = inventoryClient;
    }

    public BookResponseDTO saveBook(BookCompleteRequestDTO bookCompleteRequestDTO) {
        log.info("Book Service: Saving book started: {}", bookCompleteRequestDTO);
        Book book = bookMapper.mapRequestDtoToBook(bookCompleteRequestDTO.bookRequestDTO());
        CategoryResponseDTO categoryResponseDTO = categoryClient.getCategoryById(bookCompleteRequestDTO.bookRequestDTO().categoryId()).getBody();
        if (categoryResponseDTO == null) {
            log.error("Category with ID {} not found", bookCompleteRequestDTO.bookRequestDTO().categoryId());
            throw new IllegalStateException("Category not found");
        }
        Book savedBook = bookRepository.saveAndFlush(book);
        PriceRequestDTO priceRequestDTO = new PriceRequestDTO(savedBook.getId(),
                bookCompleteRequestDTO.priceDataDTO().priceExclVat(),
                bookCompleteRequestDTO.priceDataDTO().taxAmount(),
                bookCompleteRequestDTO.priceDataDTO().currency());
        InventoryRequestDTO inventoryRequestDTO = new InventoryRequestDTO(savedBook.getId(),
                bookCompleteRequestDTO.inventoryDataDTO().stockQuantity(),
                bookCompleteRequestDTO.inventoryDataDTO().availabilityStatus());
        PriceResponseDTO priceResponseDTO;
        InventoryResponseDTO inventoryResponseDTO;
        try {
            priceResponseDTO = priceClient.savePrice(priceRequestDTO).getBody();
            inventoryResponseDTO = inventoryClient.saveInventory(inventoryRequestDTO).getBody();
            if (priceResponseDTO == null || inventoryResponseDTO == null) {
                deleteBookById(savedBook.getId());
                throw new RuntimeException("Price or Inventory response is null");
            }
        } catch (Exception e) {
            log.error("Failed to create price/inventory", e);
            throw new RuntimeException("Book creation failed", e);
        }
        log.info("Book Service: Saving book finished: {}", savedBook.getId());
        return bookMapper.mapBookToBookResponseDTO(savedBook, categoryResponseDTO, priceResponseDTO, inventoryResponseDTO);
    }

    public BookResponseDTO getBookById(Long id) {
        log.info("Book Service: Request to get Book by id: {}", id);
        Book book = bookRepository.getReferenceById(id);
        PriceResponseDTO priceResponseDTO = priceClient.getPriceByBookId(book.getId()).getBody();
        InventoryResponseDTO inventoryResponseDTO = inventoryClient.getInventoryByBookId(book.getId()).getBody();
        CategoryResponseDTO categoryResponseDTO = categoryClient.getCategoryById(book.getCategoryId()).getBody();
        assert categoryResponseDTO != null;
        assert priceResponseDTO != null;
        assert inventoryResponseDTO != null;
        return bookMapper.mapBookToBookResponseDTO(book, categoryResponseDTO, priceResponseDTO, inventoryResponseDTO);
    }

    public void deleteBookById( Long id) {
        log.info("Book Service: Request to delete Book by id: {}", id);
        bookRepository.getReferenceById(id);
        priceClient.deletePriceByBookId(id);
        inventoryClient.deleteInventoryByBookId(id);
        bookRepository.deleteById(id);
    }

    public boolean bookExists(Long id) {
        log.info("Book Service: Checking if book exists with id: {}", id);
        return bookRepository.existsById(id);
    }

    public BookResponseDTO updateBook(Long id, BookRequestDTO bookRequestDTO) {
        log.info("Book Service: Updating book with id: {} - {}", id, bookRequestDTO);
        Book existingBook = bookRepository.getReferenceById(id);
        existingBook.setName(bookRequestDTO.name());
        existingBook.setDescription(bookRequestDTO.description());
        existingBook.setBookCoverImage(bookRequestDTO.bookCoverImage());
        existingBook.setUniqueProductCode(bookRequestDTO.uniqueProductCode());
        existingBook.setCategoryId(bookRequestDTO.categoryId());
        Book updatedBook = bookRepository.saveAndFlush(existingBook);
        CategoryResponseDTO categoryResponseDTO = categoryClient.getCategoryById(bookRequestDTO.categoryId()).getBody();
        if (categoryResponseDTO == null) {
            log.error("Category with ID {} not found", bookRequestDTO.categoryId());
        }
        InventoryResponseDTO inventoryResponseDTO;
        PriceResponseDTO priceResponseDTO;
        try{
            inventoryResponseDTO = inventoryClient.getInventoryByBookId(updatedBook.getId()).getBody();
            priceResponseDTO = priceClient.getPriceByBookId(updatedBook.getId()).getBody();
            if(priceResponseDTO == null || inventoryResponseDTO == null) {
                throw new IllegalStateException("Book data inconsistency: Price or Inventory not found");
            }
        }catch (Exception e){
            log.error("Failed to update price/inventory", e);
            throw new RuntimeException("Book update failed", e);
        }
        log.info("Book Service: Book updated successfully: {}", updatedBook);
        return bookMapper.mapBookToBookResponseDTO(updatedBook, categoryResponseDTO,  priceResponseDTO, inventoryResponseDTO );
    }

    public Page<BookResponseDTO> getAllBooks(int page, int size, Long categoryId) {
        log.info("Book Service: Getting All Books started - page: {}, size: {}, categoryId: {}", page, size, categoryId);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Book> bookPage = (categoryId != null)
                ? bookRepository.findByCategoryId(categoryId, pageRequest)
                : bookRepository.findAll(pageRequest);

        return bookPage.map(book -> {
            PriceResponseDTO priceResponseDTO = priceClient.getPriceByBookId(book.getId()).getBody();
            InventoryResponseDTO inventoryResponseDTO = inventoryClient.getInventoryByBookId(book.getId()).getBody();
            CategoryResponseDTO categoryResponseDTO = categoryClient.getCategoryById(book.getCategoryId()).getBody();
            return bookMapper.mapBookToBookResponseDTO(book, categoryResponseDTO, priceResponseDTO, inventoryResponseDTO);
        });
    }
}
