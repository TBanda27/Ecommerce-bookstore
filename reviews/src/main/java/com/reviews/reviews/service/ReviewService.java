package com.reviews.reviews.service;

import com.reviews.reviews.dto.BookResponseDTO;
import com.reviews.reviews.dto.ReviewRequestDTO;
import com.reviews.reviews.dto.ReviewResponseDTO;
import com.reviews.reviews.dto.UserResponseDTO;
import com.reviews.reviews.entity.Review;
import com.reviews.reviews.exceptions.BookNotFoundException;
import com.reviews.reviews.exceptions.DuplicateReviewException;
import com.reviews.reviews.exceptions.ReviewNotFoundException;
import com.reviews.reviews.exceptions.UserNotFoundException;
import com.reviews.reviews.feignclients.BookClient;
import com.reviews.reviews.feignclients.UserClient;
import com.reviews.reviews.mapper.ReviewMapper;
import com.reviews.reviews.repository.ReviewsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewsRepository reviewsRepository;
    private final ReviewMapper reviewMapper;
    private final UserClient userClient;
    private final BookClient bookClient;

    public ReviewService(ReviewsRepository reviewsRepository, ReviewMapper reviewMapper, UserClient userClient, BookClient bookClient) {
        this.reviewsRepository = reviewsRepository;
        this.reviewMapper = reviewMapper;
        this.userClient = userClient;
        this.bookClient = bookClient;
    }

    public ReviewResponseDTO createReview(ReviewRequestDTO reviewRequestDTO, Long userId) {
        UserResponseDTO user = userClient.getUserById(userId).getBody();
        if (user == null) {
            throw new UserNotFoundException("Invalid user ID: " + userId);
        }
        BookResponseDTO book = bookClient.getBookById(reviewRequestDTO.bookId()).getBody();
        if (book == null) {
            throw new BookNotFoundException("Invalid book ID: " + reviewRequestDTO.bookId());
        }
        if (reviewsRepository.existsByBookIdAndReviewerId(book.bookId(), userId)) {
            throw new DuplicateReviewException("You already reviewed this book");
        }

        Review review = Review.builder()
                .bookId(reviewRequestDTO.bookId())
                .bookName(book.name())
                .reviewerId(user.id())
                .reviewerName(user.username())
                .rating(reviewRequestDTO.rating())
                .review(reviewRequestDTO.review())
                .createdAt(new java.util.Date())
                .updatedAt(new java.util.Date())
                .build();

        reviewsRepository.saveAndFlush(review);
        return reviewMapper.toReviewResponseDTO(review);
    }

    public ReviewResponseDTO getReviewById(Long id) {
        Review review = reviewsRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));

        return reviewMapper.toReviewResponseDTO(review);
    }

    public Page<ReviewResponseDTO> getAllReviews(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Review> reviewsPage = reviewsRepository.findAll(pageRequest);

        return reviewsPage.map(reviewMapper::toReviewResponseDTO);
    }

    public ReviewResponseDTO updateReview(Long id, ReviewRequestDTO reviewRequestDTO) {
        Review existingReview = reviewsRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));

        existingReview.setRating(reviewRequestDTO.rating());
        existingReview.setReview(reviewRequestDTO.review());
        existingReview.setUpdatedAt(new java.util.Date());

        reviewsRepository.save(existingReview);
        return reviewMapper.toReviewResponseDTO(existingReview);
    }

    public void deleteReviewById(Long id) {
        if (!reviewsRepository.existsById(id)) {
            throw new ReviewNotFoundException("Review not found with id: " + id);
        }
        reviewsRepository.deleteById(id);
    }

    public Page<ReviewResponseDTO> getBookReviews(Long bookId, int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Review> reviewsPage = reviewsRepository.findByBookId(bookId, pageRequest);

        return reviewsPage.map(reviewMapper::toReviewResponseDTO);
    }
}
