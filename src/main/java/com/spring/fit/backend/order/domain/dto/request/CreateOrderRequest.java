package com.spring.fit.backend.order.domain.dto.request;

import com.spring.fit.backend.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    private Long shippingAddressId;

    private PaymentMethod paymentMethod;

    private String note;

    private List<OrderDetailRequest> orderDetails;

    private Long subtotalAmount;

    private Long discountAmount;

    private Long shippingFee;

    private Long totalAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailRequest {
        private Long productDetailId;

        private Integer quantity;

    }
}

