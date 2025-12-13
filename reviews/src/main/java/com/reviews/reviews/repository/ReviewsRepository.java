package com.reviews.reviews.repository;

import com.reviews.reviews.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewsRepository extends JpaRepository<Review, Long> {
    Page<Review> findByBookId(Long bookId, PageRequest pageRequest);
    Page<Review> findByReviewerId(Long reviewerId, PageRequest pageRequest);
    boolean existsByBookIdAndReviewerId(Long bookId, Long reviewerId);
}
