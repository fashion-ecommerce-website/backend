package com.spring.fit.backend.review.controller;

import com.spring.fit.backend.review.domain.dto.request.CreateReviewRequest;
import com.spring.fit.backend.review.domain.dto.request.UpdateReviewRequest;
import com.spring.fit.backend.review.domain.dto.response.ReviewResponse;
import com.spring.fit.backend.review.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Tạo review mới
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ReviewResponse review = reviewService.createReview(email, request);
        return ResponseEntity.ok(review);
    }

    // Lấy danh sách review của user hiện tại
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getUserReviews() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ReviewResponse> reviews = reviewService.getUserReviews(email);
        return ResponseEntity.ok(reviews);
    }

    // Lấy chi tiết review theo ID
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(
            @PathVariable @Positive(message = "Review ID must be positive") Long reviewId) {
        ReviewResponse review = reviewService.getReviewById( reviewId);
        return ResponseEntity.ok(review);
    }

    // Cập nhật review
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable @Positive(message = "Review ID must be positive") Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ReviewResponse review = reviewService.updateReview(email, reviewId, request);
        return ResponseEntity.ok(review);
    }

    // Xóa review
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable @Positive(message = "Review ID must be positive") Long reviewId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        reviewService.deleteReview(email, reviewId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products/{productDetailId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProductId(
            @PathVariable @Positive(message = "Product detail ID must be positive") Long productDetailId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProductId(productDetailId);
        return ResponseEntity.ok(reviews);
    }
}