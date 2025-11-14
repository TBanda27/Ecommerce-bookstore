package com.priceservice.price.dto;

import java.math.BigDecimal;

public record PriceRequestDTO(
        Long priceId,
        BigDecimal priceExclVat,
        BigDecimal taxAmount,
        String currency
) {
}
