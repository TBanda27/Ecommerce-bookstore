package com.priceservice.price.repository;

import com.priceservice.price.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<Price,Long> {
    Price getByBookId(Long bookId);
}
