package com.priceservice.price.service;

import com.priceservice.price.dto.PriceRequestDTO;
import com.priceservice.price.dto.PriceResponseDTO;
import com.priceservice.price.entity.Price;
import com.priceservice.price.mapper.PriceMapper;
import com.priceservice.price.repository.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PriceService {

    private final PriceRepository priceRepository;
    private final PriceMapper priceMapper;

    public PriceService(PriceRepository priceRepository, PriceMapper priceMapper) {
        this.priceRepository = priceRepository;
        this.priceMapper = priceMapper;
    }

    public PriceResponseDTO createPrice(PriceRequestDTO priceRequestDTO) {
        log.info("Price Service: Creating price: {}", priceRequestDTO);
        Price price = priceMapper.mapRequestDtoToPrice(priceRequestDTO);
        Price savedPrice = priceRepository.saveAndFlush(price);
        log.info("Price Service: Price created successfully: {}", savedPrice);
        return priceMapper.mapPriceToResponseDto(savedPrice);
    }

    public PriceResponseDTO getPriceById(Long id) {
        log.info("Price Service: Getting price by id: {}", id);
        Price price = priceRepository.getReferenceById(id);
        return priceMapper.mapPriceToResponseDto(price);
    }

    public PriceResponseDTO updatePrice(Long id, PriceRequestDTO priceRequestDTO) {
        log.info("Price Service: Updating price with id: {} - {}", id, priceRequestDTO);
        Price existingPrice = priceRepository.getReferenceById(id);

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
        Price price = priceRepository.getReferenceById(id);
        priceRepository.delete(price);
    }
}
