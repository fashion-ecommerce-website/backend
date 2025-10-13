package com.spring.fit.backend.review.service;

import com.spring.fit.backend.review.domain.dto.request.CreateReviewRequest;
import com.spring.fit.backend.review.domain.dto.request.UpdateReviewRequest;
import com.spring.fit.backend.review.domain.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(String userEmail, CreateReviewRequest request);
    List<ReviewResponse> getReviewsByProductDetail(Long productDetailId);
    ReviewResponse getReviewById(Long reviewId);
    List<ReviewResponse> getUserReviews(String userEmail);
    ReviewResponse updateReview(String userEmail,Long id, UpdateReviewRequest request) ;
    void deleteReview(String userEmail, Long reviewId);
    List<ReviewResponse> getReviewsByProductId(Long productId);
}

