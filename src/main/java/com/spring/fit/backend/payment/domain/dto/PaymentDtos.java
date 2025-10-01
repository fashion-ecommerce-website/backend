package com.spring.fit.backend.payment.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PaymentDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateCheckoutRequest {
        @NotNull
        private Long paymentId;

        private String successUrl;
        private String cancelUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckoutSessionResponse {
        private String sessionId;
        private String checkoutUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentIntentResponse {
        private String paymentIntentId;
        private String clientSecret;
        private BigDecimal amount;
        private String currency;
        private String status;
    }
}


