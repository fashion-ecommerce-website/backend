package com.spring.fit.backend.review.domain.dto.response;

import com.spring.fit.backend.review.domain.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private Long id;

    private Long userId;
    private String username;

    private Long productDetailId;

    private Short rating;
    private String content;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review review) {
        if (review == null) return null;

        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getUsername(),
                review.getProductDetail().getId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }

}

