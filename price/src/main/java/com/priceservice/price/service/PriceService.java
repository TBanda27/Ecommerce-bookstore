package com.priceservice.price.service;

import com.priceservice.price.dto.PriceRequestDTO;
import com.priceservice.price.dto.PriceResponseDTO;
import com.priceservice.price.entity.Price;
import com.priceservice.price.exceptions.BadRequestException;
import com.priceservice.price.exceptions.PriceNotFoundException;
import com.priceservice.price.feignclient.BookClient;
import com.priceservice.price.mapper.PriceMapper;
import com.priceservice.price.repository.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PriceService {

    private final PriceRepository priceRepository;
    private final PriceMapper priceMapper;
    private final BookClient bookClient;

    public PriceService(PriceRepository priceRepository, PriceMapper priceMapper, BookClient bookClient) {
        this.priceRepository = priceRepository;
        this.priceMapper = priceMapper;
        this.bookClient = bookClient;
    }

    public PriceResponseDTO createPrice(PriceRequestDTO priceRequestDTO) {
        log.info("Price Service: Creating price: {}", priceRequestDTO);

        // Validate that book exists
        try {
            ResponseEntity<Boolean> response = bookClient.checkBookExists(priceRequestDTO.bookId());
            if (response.getBody() == null || !response.getBody()) {
                throw new BadRequestException("Cannot create price: Book with ID " + priceRequestDTO.bookId() + " does not exist");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Book with ID {} not found", priceRequestDTO.bookId());
            throw new BadRequestException("Cannot create price: Book with ID " + priceRequestDTO.bookId() + " does not exist");
        }

        Price price = priceMapper.mapRequestDtoToPrice(priceRequestDTO);
        Price savedPrice = priceRepository.saveAndFlush(price);
        log.info("Price Service: Price created successfully: {}", savedPrice);
        return priceMapper.mapPriceToResponseDto(savedPrice);
    }

    public PriceResponseDTO getPriceById(Long id) {
        log.info("Price Service: Getting price by id: {}", id);
        Price price = priceRepository.findById(id)
                .orElseThrow(() -> new PriceNotFoundException("Price with id: " + id + " not found"));
        return priceMapper.mapPriceToResponseDto(price);
    }

    public PriceResponseDTO updatePrice(Long id, PriceRequestDTO priceRequestDTO) {
        log.info("Price Service: Updating price with id: {} - {}", id, priceRequestDTO);

        // Validate that book exists
        try {
            ResponseEntity<Boolean> response = bookClient.checkBookExists(priceRequestDTO.bookId());
            if (response.getBody() == null || !response.getBody()) {
                throw new BadRequestException("Cannot update price: Book with ID " + priceRequestDTO.bookId() + " does not exist");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Book with ID {} not found", priceRequestDTO.bookId());
            throw new BadRequestException("Cannot update price: Book with ID " + priceRequestDTO.bookId() + " does not exist");
        }

        Price existingPrice = priceRepository.findById(id)
                .orElseThrow(() -> new PriceNotFoundException("Price with id: " + id + " not found"));

        existingPrice.setBookId(priceRequestDTO.bookId());
        existingPrice.setPriceExclVat(priceRequestDTO.priceExclVat());
        existingPrice.setTaxAmount(priceRequestDTO.taxAmount());
        existingPrice.setCurrency(priceRequestDTO.currency());

        Price updatedPrice = priceRepository.saveAndFlush(existingPrice);
        log.info("Price Service: Price updated successfully: {}", updatedPrice);
        return priceMapper.mapPriceToResponseDto(updatedPrice);
    }

    public void deletePrice(Long id) {
        log.info("Price Service: Deleting price by id: {}", id);
        Price price = priceRepository.findById(id)
                .orElseThrow(() -> new PriceNotFoundException("Price with id: " + id + " not found"));

        // Check if book still exists - prevent delete if it does
        try {
            ResponseEntity<Boolean> response = bookClient.checkBookExists(price.getBookId());
            if (response.getBody() != null && response.getBody()) {
                log.error("Cannot delete price: Book with ID {} still exists", price.getBookId());
                throw new BadRequestException("Cannot delete price: Book with ID " + price.getBookId() + " still exists. Delete the book first.");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            // Book doesn't exist, safe to delete price
            log.info("Book not found, proceeding with price deletion");
        }

        priceRepository.delete(price);
    }

    public PriceResponseDTO getPriceByBookId(Long bookId) {
        log.info("Price Service: Getting price by book id: {}", bookId);
        Price price = priceRepository.findByBookId(bookId)
                .orElseThrow(() -> new PriceNotFoundException("Price for book id: " + bookId + " not found"));
        return priceMapper.mapPriceToResponseDto(price);
    }

    public void deleteByBookId(Long bookId) {
        log.info("Price Service: Deleting price by book id: {}", bookId);
        Price price = priceRepository.findByBookId(bookId)
                .orElseThrow(() -> new PriceNotFoundException("Price for book id: " + bookId + " not found"));
        priceRepository.delete(price);
    }
}
