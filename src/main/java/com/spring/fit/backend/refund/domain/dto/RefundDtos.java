package com.spring.fit.backend.refund.domain.dto;

import com.spring.fit.backend.common.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RefundDtos {

    private RefundDtos() {
        // Utility class - prevent instantiation
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRefundRequest {
        private Long orderId;
        private String reason;
        private BigDecimal refundAmount; // Optional, if null will use order total
        private List<String> imageUrls; // List of image URLs from Cloudinary
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefundRequestResponse {
        private Long id;
        private Long orderId;
        private Long userId;
        private String userEmail;
        private RefundStatus status;
        private String reason;
        private BigDecimal refundAmount;
        private String adminNote;
        private Long processedBy;
        private LocalDateTime processedAt;
        private String stripeRefundId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<String> imageUrls; // List of image URLs
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRefundStatusRequest {
        private RefundStatus status;
        private String adminNote;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefundRequestFilter {
        private Long userId;
        private RefundStatus status;
        private Long orderId;
    }
}

