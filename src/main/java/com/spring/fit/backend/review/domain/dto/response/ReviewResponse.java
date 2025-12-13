package com.spring.fit.backend.review.domain.dto.response;

import com.spring.fit.backend.order.domain.entity.OrderDetail;
import com.spring.fit.backend.review.domain.entity.Review;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {

    private Long id;

    private Long userId;
    private String username;

    private Long productDetailId;

    private BigDecimal rating;

    private String content;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review review) {
        if (review == null) return null;

        OrderDetail od = review.getOrderDetail();

        return new ReviewResponse(
                review.getId(),
                od.getOrder().getUser().getId(),
                od.getOrder().getUser().getUsername(),
                od.getProductDetail().getId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}

