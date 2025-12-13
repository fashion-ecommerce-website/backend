package com.spring.fit.backend.review.domain.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest {

    @NotNull
    private Long orderId;

    @NotNull
    private Long orderDetailId;

    @Column(
            nullable = false,
            precision = 2, // tổng số chữ số
            scale = 2      // số chữ số thập phân
    )
    private BigDecimal rating;

    private String content;
}

