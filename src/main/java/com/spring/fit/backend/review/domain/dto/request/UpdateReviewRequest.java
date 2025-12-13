package com.spring.fit.backend.review.domain.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateReviewRequest {
    @NotNull(message = "rating cannot be null")
    @Min(value = 1, message = "rating must be >= 1")
    @Max(value = 5, message = "rating must be <= 5")
    @Column(
            nullable = false,
            precision = 2, // tổng số chữ số
            scale = 2      // số chữ số thập phân
    )
    private BigDecimal rating;
    private String content;

}
