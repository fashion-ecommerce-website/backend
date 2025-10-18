package com.spring.fit.backend.review.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import com.spring.fit.backend.review.domain.dto.request.CreateReviewRequest;
import com.spring.fit.backend.review.domain.dto.request.UpdateReviewRequest;
import com.spring.fit.backend.review.domain.dto.response.ReviewResponse;
import com.spring.fit.backend.review.domain.entity.Review;
import com.spring.fit.backend.review.repository.ReviewRepository;
import com.spring.fit.backend.review.service.ReviewService;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductDetailRepository productDetailRepository;

    @Override
    public ReviewResponse createReview(String userEmail, CreateReviewRequest request) {
        log.info("Inside ReviewServiceImpl.createReview userEmail={}, request={}", userEmail, request);

        // Tìm user
        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        // Tìm product detail
        ProductDetail productDetail = productDetailRepository.findById(request.getProductDetailId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found"));

        // Tạo entity review
        Review review = Review.builder()
                .user(user)
                .productDetail(productDetail)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        Review savedReview = reviewRepository.save(review);

        log.info("Inside ReviewServiceImpl.createReview success userId={}, reviewId={}", user.getId(), savedReview.getId());
        return ReviewResponse.fromEntity(savedReview);
    }
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProductDetail(Long productDetailId) {
        log.info("Inside ReviewServiceImpl.getReviewsByProductDetail productDetailId={}", productDetailId);

        List<Review> reviews = reviewRepository.findByProductDetailIdOrderByCreatedAtDesc(productDetailId);
        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long reviewId) {
        log.info("Inside ReviewServiceImpl.getReviewById reviewId={}", reviewId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Review not found"));
        return ReviewResponse.fromEntity(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getUserReviews(String userEmail) {
        log.info("Inside ReviewServiceImpl.getUserReviews userEmail={}", userEmail);

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public ReviewResponse updateReview(String userEmail,Long id, UpdateReviewRequest request) {

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ErrorException(HttpStatus.FORBIDDEN, "You are not allowed to update this review");
        }

        review.setRating(request.getRating());
        review.setContent(request.getContent());

        Review updated = reviewRepository.save(review);

        log.info("Inside ReviewServiceImpl.updateReview success userId={}, reviewId={}", user.getId(), updated.getId());
        return ReviewResponse.fromEntity(updated);
    }

    @Override
    public void deleteReview(String userEmail, Long reviewId) {
        log.info("Inside ReviewServiceImpl.deleteReview userEmail={}, reviewId={}", userEmail, reviewId);

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ErrorException(HttpStatus.FORBIDDEN, "You are not allowed to delete this review");
        }

        reviewRepository.delete(review);
        log.info("Inside ReviewServiceImpl.deleteReview success userId={}, reviewId={}", user.getId(), reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProductId(Long productDetailId) {
        log.info("Inside ReviewServiceImpl.getReviewsByProductId productDetailId={}", productDetailId);
        ProductDetail productDetail = productDetailRepository.findById(productDetailId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found"));
        Long productId = productDetail.getProduct().getId();
        List<Review> reviews = reviewRepository.findAllByProductId(productId);
        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

}

