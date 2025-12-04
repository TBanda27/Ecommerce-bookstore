package com.reviews.reviews.mapper;

import com.reviews.reviews.dto.ReviewResponseDTO;
import com.reviews.reviews.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponseDTO toReviewResponseDTO(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getBookId(),
                review.getBookName(),
                review.getRating(),
                review.getReview(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                review.getReviewerName()
        );
    }
}
