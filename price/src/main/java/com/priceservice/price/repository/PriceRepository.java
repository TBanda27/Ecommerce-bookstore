package com.priceservice.price.repository;

import com.priceservice.price.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price,Long> {
    Optional<Price> findByBookId(Long bookId);
}
