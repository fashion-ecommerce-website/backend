package com.spring.fit.backend.order.domain.dto.request;

import com.spring.fit.backend.common.enums.FulfillmentStatus;
import com.spring.fit.backend.common.enums.PaymentStatus;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderRequest {

    private FulfillmentStatus status;

    private PaymentStatus paymentStatus;

    private String currency;

    @Positive(message = "Subtotal amount must be positive")
    private BigDecimal subtotalAmount;

    @Positive(message = "Discount amount must be positive")
    private BigDecimal discountAmount;

    @Positive(message = "Shipping fee must be positive")
    private BigDecimal shippingFee;

    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    private String note;
}

