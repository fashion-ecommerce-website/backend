package com.spring.fit.backend.review.service.impl;

import com.spring.fit.backend.common.enums.FulfillmentStatus;
import com.spring.fit.backend.common.enums.OrderStatus;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.domain.dto.response.OrderResponse;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.domain.entity.OrderDetail;
import com.spring.fit.backend.order.repository.OrderRepository;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import com.spring.fit.backend.review.domain.dto.request.CreateReviewRequest;
import com.spring.fit.backend.review.domain.dto.request.UpdateReviewRequest;
import com.spring.fit.backend.review.domain.dto.response.ReviewModerationResponse;
import com.spring.fit.backend.review.domain.dto.response.ReviewResponse;
import com.spring.fit.backend.review.domain.entity.Review;
import com.spring.fit.backend.review.repository.ReviewRepository;
import com.spring.fit.backend.review.service.ReviewService;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.moderation.ModerationResult;
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
    private final OrderRepository orderRepository;
    private final AIModerationServiceImpl aiModerationService;

    @Override
    public ReviewResponse createReview(String userEmail, CreateReviewRequest request) {

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        Long orderDetailId = request.getOrderDetailId();

        Order order = orderRepository.findByIdWithDetails(request.getOrderId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderDetail orderDetail = order.getOrderDetails().stream()
                .filter(od -> od.getId().equals(request.getOrderDetailId()))
                .findFirst()
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Order detail not found"));


        if (!order.getUser().getId().equals(user.getId())) {
            throw new ErrorException(HttpStatus.FORBIDDEN, "Not your order");
        }
        if (order.getStatus() != FulfillmentStatus.FULFILLED) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Order not completed");
        }


        if (orderDetail.getReview() != null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Already reviewed");
        }

        ReviewModerationResponse moderation = aiModerationService.verifyContent(request.getContent());

        if (!moderation.isClean()) {
            throw new ErrorException(HttpStatus.BAD_REQUEST,
                    "This content violates community guidelines. Please use polite language.");
        }

        Review review = Review.builder()
                .orderDetail(orderDetail)
                .rating(request.getRating())
                .content(request.getContent()) // Dùng kết quả từ AI thay vì request.getContent()
                .build();

        Review saved = reviewRepository.save(review);
        return ReviewResponse.fromEntity(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProductDetail(Long productDetailId) {

        List<Review> reviews =
                reviewRepository.findReviewsByProductDetailId(productDetailId);

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

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        return reviewRepository.findUserReviews(user.getId())
                .stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }
    @Override
    public ReviewResponse updateReview(String userEmail, Long id, UpdateReviewRequest request) {

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Review not found"));

        Long ownerId = review.getOrderDetail()
                .getOrder()
                .getUser()
                .getId();

        if (!ownerId.equals(user.getId())) {
            throw new ErrorException(HttpStatus.FORBIDDEN, "You are not allowed to update this review");
        }

        ReviewModerationResponse moderation = aiModerationService.verifyContent(request.getContent());
        if (!moderation.isClean()) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Nội dung cập nhật vi phạm quy chuẩn");
        }

        review.setRating(request.getRating());
        review.setContent(request.getContent());

        return ReviewResponse.fromEntity(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(String userEmail, Long reviewId) {

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Review not found"));

        Long ownerId = review.getOrderDetail()
                .getOrder()
                .getUser()
                .getId();

        if (!ownerId.equals(user.getId())) {
            throw new ErrorException(HttpStatus.FORBIDDEN, "You are not allowed to delete this review");
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProductId(Long productId) {

        return reviewRepository.findAllByProductId(productId)
                .stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

}

