package com.priceservice.price.mapper;

import com.priceservice.price.dto.PriceRequestDTO;
import com.priceservice.price.dto.PriceResponseDTO;
import com.priceservice.price.entity.Price;
import org.springframework.stereotype.Component;

@Component
public class PriceMapper {

    public Price mapRequestDtoToPrice(PriceRequestDTO priceRequestDTO) {
        return Price.builder()
                .bookId(priceRequestDTO.bookId())
                .priceExclVat(priceRequestDTO.priceExclVat())
                .taxAmount(priceRequestDTO.taxAmount())
                .currency(priceRequestDTO.currency())
                .build();
    }

    public PriceResponseDTO mapPriceToResponseDto(Price price) {
        return new PriceResponseDTO(
                price.getId(),
                price.getBookId(),
                price.getPriceExclVat(),
                price.getPriceExclVat().add(price.getTaxAmount()),
                price.getTaxAmount(),
                price.getCurrency()
        );
    }
}
