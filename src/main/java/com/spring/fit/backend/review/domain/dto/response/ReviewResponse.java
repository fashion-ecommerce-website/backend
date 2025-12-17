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
    private String avatarUrl;

    private Long productDetailId;

    private BigDecimal rating;

    private String content;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review review) {
        if (review == null) return null;

        OrderDetail od = review.getOrderDetail();
        var user = od.getOrder().getUser();

        return new ReviewResponse(
                review.getId(),
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl(),
                od.getProductDetail().getId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}

