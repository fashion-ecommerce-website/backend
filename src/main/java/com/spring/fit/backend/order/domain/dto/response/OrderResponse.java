package com.spring.fit.backend.order.domain.dto.response;

import com.spring.fit.backend.common.enums.FulfillmentStatus;
import com.spring.fit.backend.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userUsername;
    private FulfillmentStatus status;
    private PaymentStatus paymentStatus;
    private String currency;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String note;
    private AddressResponse shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderDetailResponse> orderDetails;
    private List<PaymentResponse> payments;
    private List<ShipmentResponse> shipments;
    private String voucherCode;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressResponse {
        private Long id;
        private String fullName;
        private String phone;
        private String line;
        private String ward;
        private String city;
        private String countryCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailResponse {
        private Long id;
        private Long productDetailId;
        private String title;
        private String colorLabel;
        private String sizeLabel;
        private Integer quantity;
        private BigDecimal unitPrice;        // base price
        private BigDecimal finalPrice;       // after promotion
        private Integer percentOff;          // integer percent
        private Long promotionId;            // nullable
        private String promotionName;        // nullable
        private BigDecimal totalPrice;
        private List<String> images;         // list of image URLs for product detail
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponse {
        private Long id;
        private String method;
        private String status;
        private BigDecimal amount;
        private String provider;
        private String transactionNo;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipmentResponse {
        private Long id;
        private String carrier;
        private String trackingNo;
        private String status;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime createdAt;
    }
}

