package com.spring.fit.backend.review.domain.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateReviewRequest {
    @NotNull(message = "rating cannot be null")
    @Column(
            nullable = false,
            precision = 2, // tổng số chữ số
            scale = 2      // số chữ số thập phân
    )
    private BigDecimal rating;
    private String content;

}
