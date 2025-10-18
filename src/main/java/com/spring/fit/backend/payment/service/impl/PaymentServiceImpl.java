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
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements com.spring.fit.backend.payment.service.PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final StripeProperties stripeProperties;

    @Override
    public CheckoutSessionResponse createCheckoutSessionFromContext(CreateCheckoutRequest request) {
        Long paymentId = request.getPaymentId();
        if (paymentId == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "paymentId is required");
        }

        paymentRepository.findUserIdByPaymentId(paymentId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Payment not found"));

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

            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Payment not found"));
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
        order.setPaymentStatus(PaymentStatus.PAID);
        Payment payment = paymentRepository.findFirstByOrder_IdOrderByIdDesc(orderId)
                .orElse(Payment.builder().order(order).amount(order.getTotalAmount()).build());
        payment.setProvider(provider);
        payment.setTransactionNo(transactionNo);
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
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
}


