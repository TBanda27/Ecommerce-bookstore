package com.reviews.reviews.controller;


import com.reviews.reviews.dto.ReviewRequestDTO;
import com.reviews.reviews.dto.ReviewResponseDTO;
import com.reviews.reviews.exceptions.ErrorDetails;
import com.reviews.reviews.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/review")
@Slf4j
@Tag(name = "Review Management", description = "APIs for managing book reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(
            summary = "Create a new review",
            description = "Creates a new review for a book. User can only review a book once. " +
                    "Requires user ID in X-User-Id header."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Review created successfully",
                    content = @Content(schema = @Schema(implementation = ReviewResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or user already reviewed this book",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book or user not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Parameter(description = "Review details including bookId, rating (0-5), and review text (3-500 chars)", required = true)
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO,
            @Parameter(description = "User ID of the reviewer", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Review Controller: createReview called with reviewRequestDTO: {}", reviewRequestDTO);
        return new ResponseEntity<>(reviewService.createReview(reviewRequestDTO, userId), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get review by ID",
            description = "Retrieves a specific review by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Review found",
                    content = @Content(schema = @Schema(implementation = ReviewResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getReviewById(
            @Parameter(description = "Review ID", required = true)
            @PathVariable Long id) {
        log.info("Review Controller: getReviewById called with id: {}", id);
        return new ResponseEntity<>(reviewService.getReviewById(id), HttpStatus.OK);
    }

    @Operation(
            summary = "Get all reviews",
            description = "Retrieves a paginated list of all reviews with sorting options. " +
                    "Sortable fields: id, rating, createdAt, updatedAt, bookId, reviewerName"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved reviews"
            )
    })
    @GetMapping
    public ResponseEntity<Page<ReviewResponseDTO>> getAllReviews(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Review Controller: getAllReviews called with page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        return new ResponseEntity<>(reviewService.getAllReviews(page, size, sortBy, sortDir), HttpStatus.OK);
    }

    @Operation(
            summary = "Get reviews by book ID",
            description = "Retrieves all reviews for a specific book with pagination and sorting"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved book reviews"
            )
    })
    @GetMapping("/book/{bookId}")
    public ResponseEntity<Page<ReviewResponseDTO>> getBookReviews(
            @Parameter(description = "Book ID", required = true)
            @PathVariable Long bookId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Review Controller: getBookReviews called with bookId: {}, page: {}, size: {}, sortBy: {}, sortDir: {}", bookId, page, size, sortBy, sortDir);
        return new ResponseEntity<>(reviewService.getBookReviews(bookId, page, size, sortBy, sortDir), HttpStatus.OK);
    }

    @Operation(
            summary = "Update a review",
            description = "Updates an existing review's rating and text. The updatedAt timestamp is automatically updated."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Review updated successfully",
                    content = @Content(schema = @Schema(implementation = ReviewResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @Parameter(description = "Review ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated review details (rating and review text)", required = true)
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO) {
        log.info("Review Controller: updateReview called with id: {}, reviewRequestDTO: {}", id, reviewRequestDTO);
        return new ResponseEntity<>(reviewService.updateReview(id, reviewRequestDTO), HttpStatus.OK);
    }

    @Operation(
            summary = "Delete a review",
            description = "Permanently deletes a review by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Review deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review ID", required = true)
            @PathVariable Long id) {
        log.info("Review Controller: deleteReview called with id: {}", id);
        reviewService.deleteReviewById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
