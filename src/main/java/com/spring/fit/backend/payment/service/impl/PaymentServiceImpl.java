package com.spring.fit.backend.payment.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.fit.backend.common.enums.PaymentStatus;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.repository.OrderRepository;
import com.spring.fit.backend.payment.config.StripeProperties;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.CheckoutSessionResponse;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.CreateCheckoutRequest;
import com.spring.fit.backend.payment.domain.entity.Payment;
import com.spring.fit.backend.payment.repository.PaymentRepository;
import com.spring.fit.backend.payment.service.PaymentService;
import com.spring.fit.backend.voucher.domain.dto.VoucherValidateRequest;
import com.spring.fit.backend.voucher.domain.dto.VoucherValidateResponse;
import com.spring.fit.backend.voucher.domain.entity.Voucher;
import com.spring.fit.backend.voucher.domain.entity.VoucherUsage;
import com.spring.fit.backend.voucher.repository.VoucherUsageRepository;
import com.spring.fit.backend.voucher.service.VoucherService;
import com.spring.fit.backend.promotion.service.OrderDetailPromotionService;
import com.spring.fit.backend.common.enums.VoucherType;
import com.spring.fit.backend.common.enums.VoucherUsageStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final StripeProperties stripeProperties;
    private final VoucherUsageRepository voucherUsageRepository;
    private final VoucherService voucherService;
    private final OrderDetailPromotionService orderDetailPromotionService;

    @Override
    public CheckoutSessionResponse createCheckoutSessionFromContext(CreateCheckoutRequest request) {
        Long paymentId = request.getPaymentId();
        if (paymentId == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "paymentId is required");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Inside PaymentServiceImpl.createCheckoutSessionFromContext, payment not found with id: " + paymentId));

        if(payment.getOrder().getVoucher() != null) {
            VoucherValidateResponse voucherResponse = voucherService.validateVoucher(VoucherValidateRequest.builder()
                    .code(payment.getOrder().getVoucher().getCode())
                    .subtotal(payment.getOrder().getSubtotalAmount().doubleValue())
                    .build(), payment.getOrder().getUser().getId());
            if(!voucherResponse.isValid()) {
                throw new ErrorException(HttpStatus.BAD_REQUEST, voucherResponse.getMessage());
            }
        }
        // build line items from native query
        var rows = paymentRepository.findOrderAndItemsByPaymentId(paymentId);
        if (rows.isEmpty()) {
            throw new ErrorException(HttpStatus.NOT_FOUND, "Order items not found for payment");
        }

        // Extract order-level data from first row
        Object[] first = rows.get(0);
        Long orderId = ((Number) first[0]).longValue();
        // totalAmount available if needed
        // java.math.BigDecimal totalAmount = (java.math.BigDecimal) first[1];
        String currency = (String) first[2];

        String successUrl = request.getSuccessUrl();
        String cancelUrl = request.getCancelUrl();
        if (successUrl == null || successUrl.isBlank()) {
            successUrl = stripeProperties.getSuccessUrl();
        }
        if (successUrl == null || successUrl.isBlank()) {
            successUrl = "http://localhost:3000/payment/success?session_id={CHECKOUT_SESSION_ID}";
        }
        if (cancelUrl == null || cancelUrl.isBlank()) {
            cancelUrl = stripeProperties.getCancelUrl();
        }
        if (cancelUrl == null || cancelUrl.isBlank()) {
            cancelUrl = "http://localhost:3000/payment/cancel";
        }

        try {
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putMetadata("orderId", String.valueOf(orderId))
                    .putMetadata("paymentId", String.valueOf(paymentId));

            for (Object[] row : rows) {
                String title = (String) row[3];
                String color = (String) row[4];
                String size = (String) row[5];
                Long quantity = ((Number) row[6]).longValue();
                java.math.BigDecimal unitPrice = (java.math.BigDecimal) row[7];

                String name = title;
                if (color != null) name += " - " + color;
                if (size != null) name += " - " + size;

                SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData
                        .builder().setName(name).build();

                // Stripe expects amount in the smallest currency unit (no decimals for VND)
                long unitAmount = unitPrice.movePointRight(0).longValue();

                SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(currency.toLowerCase())
                        .setUnitAmount(unitAmount)
                        .setProductData(productData)
                        .build();

                SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                        .setQuantity(quantity)
                        .setPriceData(priceData)
                        .build();

                paramsBuilder.addLineItem(lineItem);
            }

            Session session = Session.create(paramsBuilder.build());

            payment.setProvider("STRIPE");
            payment.setTransactionNo(session.getId());
            payment.setStatus(PaymentStatus.UNPAID);
            paymentRepository.save(payment);

            return CheckoutSessionResponse.builder()
                    .sessionId(session.getId())
                    .checkoutUrl(session.getUrl())
                    .build();
        } catch (StripeException e) {
            log.error("Failed to create Stripe Checkout session", e);
            throw new ErrorException(HttpStatus.BAD_GATEWAY, "Stripe error: " + e.getMessage());
        }
    }

    @Override
    public void handlePaymentSucceeded(Long orderId, String provider, String transactionNo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
        // 1: Apply voucher if voucher was applied to the order
        if (order.getVoucher() != null) {
            try {
                voucherService.applyVoucher(VoucherValidateRequest.builder()
                        .code(order.getVoucher().getCode())
                        .subtotal(order.getSubtotalAmount().doubleValue())
                        .build(), order.getUser().getId(), order.getId());
            } catch (Exception e) {
                throw new ErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }

        // 2: Create OrderDetailPromotion records for order details with promotionId
        try {
            orderDetailPromotionService.createPromotionsForOrder(order);
            log.info("Inside PaymentServiceImpl.handlePaymentSucceeded, Successfully created promotions for order: {}", orderId);
        } catch (Exception e) {
            log.error("Inside PaymentServiceImpl.handlePaymentSucceeded, Failed to create promotions for order: {}", orderId, e);
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Failed to create promotions: " + e.getMessage());
        }

        // 3: Set payment status to PAID
        order.setPaymentStatus(PaymentStatus.PAID);
        Payment payment = paymentRepository.findFirstByOrder_IdOrderByIdDesc(orderId)
                .orElse(Payment.builder().order(order).amount(order.getTotalAmount()).build());
        payment.setProvider(provider);
        payment.setTransactionNo(transactionNo);
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
        
        // Create VoucherUsage if voucher was applied to the order
        if (order.getVoucher() != null) {
            createVoucherUsage(order);
        }
        
        orderRepository.save(order);
    }

    @Override
    public void handlePaymentFailed(Long orderId, String provider, String transactionNo, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
        order.setPaymentStatus(PaymentStatus.UNPAID);
        Payment payment = paymentRepository.findFirstByOrder_IdOrderByIdDesc(orderId)
                .orElse(Payment.builder().order(order).amount(order.getTotalAmount()).build());
        payment.setProvider(provider);
        payment.setTransactionNo(transactionNo);
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    @Override
    public void handleStripeEvent(Event event) {
        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
            case "checkout.session.expired" -> handleCheckoutSessionExpired(event);
            case "charge.refunded", "refund.created", "charge.refund.updated" -> handleRefunded(event);
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = extractSession(event);
        if (session == null || session.getMetadata() == null) {
            return;
        }
        String orderIdStr = session.getMetadata().get("orderId");
        if (orderIdStr == null) {
            return;
        }
        try {
            Long orderId = Long.parseLong(orderIdStr);
            handlePaymentSucceeded(orderId, "STRIPE", session.getId());
        } catch (NumberFormatException ex) {
            log.warn("Invalid orderId in checkout.session.completed: {}", orderIdStr);
        }
    }

    private void handleCheckoutSessionExpired(Event event) {
        Session session = extractSession(event);
        if (session == null || session.getMetadata() == null) {
            return;
        }
        String orderIdStr = session.getMetadata().get("orderId");
        if (orderIdStr == null) {
            return;
        }
        try {
            Long orderId = Long.parseLong(orderIdStr);
            handlePaymentFailed(orderId, "STRIPE", session.getId(), "SESSION_EXPIRED");
        } catch (NumberFormatException ex) {
            log.warn("Invalid orderId in checkout.session.expired: {}", orderIdStr);
        }
    }

    private void handleRefunded(Event event) {
        Session session = extractSession(event);
        if (session == null || session.getMetadata() == null) {
            // For non-session events, we might not be able to extract Session; skip minimal impl
            return;
        }
        String orderIdStr = session.getMetadata().get("orderId");
        if (orderIdStr == null) {
            return;
        }
        try {
            Long orderId = Long.parseLong(orderIdStr);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            Payment payment = paymentRepository.findFirstByOrder_IdOrderByIdDesc(orderId)
                    .orElse(Payment.builder().order(order).amount(order.getTotalAmount()).build());
            payment.setProvider("STRIPE");
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            orderRepository.save(order);
        } catch (NumberFormatException ex) {
            log.warn("Invalid orderId in refund event: {}", orderIdStr);
        }
    }

    private Session extractSession(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        return (Session) deserializer.getObject().orElse(null);
    }

    private void createVoucherUsage(Order order) {
        try {
            Voucher voucher = order.getVoucher();
            if (voucher == null) {
                log.warn("No voucher found for order {}", order.getId());
                return;
            }

            // Calculate discount amount
            BigDecimal discountAmount = calculateVoucherDiscount(voucher, order.getSubtotalAmount());

            // Create VoucherUsage
            var voucherUsage = VoucherUsage.builder()
                    .voucher(voucher)
                    .user(order.getUser())
                    .order(order)
                    .discountAmount(discountAmount)
                    .status(VoucherUsageStatus.APPLIED)
                    .build();

            voucherUsageRepository.save(voucherUsage);
            log.info("VoucherUsage created for order {} with voucher code {}", order.getId(), voucher.getCode());
        } catch (Exception e) {
            log.error("Failed to create VoucherUsage for order {} with voucher", order.getId(), e);
            // Don't throw exception to avoid breaking payment success flow
        }
    }

    private BigDecimal calculateVoucherDiscount(Voucher voucher, BigDecimal subtotalAmount) {
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        if (voucher.getType() == VoucherType.PERCENT) {
            // Calculate percentage discount
            discountAmount = subtotalAmount.multiply(voucher.getValue().divide(BigDecimal.valueOf(100)));
            
            // Apply max discount limit if set
            if (voucher.getMaxDiscount() != null && discountAmount.compareTo(voucher.getMaxDiscount()) > 0) {
                discountAmount = voucher.getMaxDiscount();
            }
        } else if (voucher.getType() == VoucherType.FIXED) {
            // Fixed amount discount
            discountAmount = voucher.getValue();
        }
        
        // Ensure discount doesn't exceed subtotal
        if (discountAmount.compareTo(subtotalAmount) > 0) {
            discountAmount = subtotalAmount;
        }
        
        return discountAmount;
    }
}


