package com.spring.fit.backend.payment.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.fit.backend.common.enums.PaymentStatus;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.repository.OrderRepository;
import com.spring.fit.backend.order.service.InventoryService;
import com.spring.fit.backend.order.service.ShipmentService;
import com.spring.fit.backend.payment.config.StripeProperties;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.CheckoutSessionResponse;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.CreateCheckoutRequest;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.RefundRequest;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.RefundResponse;
import com.spring.fit.backend.payment.domain.entity.Payment;
import com.spring.fit.backend.payment.repository.PaymentRepository;
import com.spring.fit.backend.payment.service.PaymentService;
import com.spring.fit.backend.voucher.domain.dto.VoucherValidateRequest;
import com.spring.fit.backend.voucher.domain.dto.VoucherValidateResponse;
import com.spring.fit.backend.voucher.domain.entity.Voucher;
import com.spring.fit.backend.voucher.domain.entity.VoucherUsage;
import com.spring.fit.backend.voucher.repository.VoucherUsageRepository;
import com.spring.fit.backend.voucher.service.VoucherService;
import com.spring.fit.backend.email.service.OrderEmailService;
import com.spring.fit.backend.payment.helper.StripeHelper;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionApplyRequest;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionApplyResponse;
import com.spring.fit.backend.promotion.service.OrderDetailPromotionService;
import com.spring.fit.backend.promotion.service.PromotionService;
import com.spring.fit.backend.common.enums.VoucherUsageStatus;
import com.spring.fit.backend.user.event.PaymentSuccessEvent;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.spring.fit.backend.common.constants.OrderConstants.*;
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
    private final PromotionService promotionService;
    private final OrderEmailService orderEmailService;
    private final StripeHelper stripeHelper;
    private final ShipmentService shipmentService;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CheckoutSessionResponse createCheckoutSessionFromContext(CreateCheckoutRequest request) {
        // 1. VALIDATION & DATA RETRIEVAL
        Long paymentId = request.getPaymentId();
        if (paymentId == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "paymentId is required");
        }

        // Retrieve payment entity and validate it exists
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Inside PaymentServiceImpl.createCheckoutSessionFromContext, payment not found with id: " + paymentId));

        // 2. ORDER DATA EXTRACTION
        var rows = paymentRepository.findOrderAndItemsByPaymentId(paymentId);
        if (rows.isEmpty()) {
            throw new ErrorException(HttpStatus.NOT_FOUND, "Order items not found for payment");
        }

        Object[] first = rows.get(0);
        Long orderId = ((Number) first[0]).longValue();
        BigDecimal totalAmount = (BigDecimal) first[1];
        BigDecimal discountAmount = (BigDecimal) first[3];
        BigDecimal shippingFee = (BigDecimal) first[4];
        String currency = (String) first[5];

        log.info("Inside PaymentServiceImpl.createCheckoutSessionFromContext, Total amount: {}, Discount amount: {} , Shipping fee: {}", 
        totalAmount, discountAmount, shippingFee);

        // 3. URL CONFIGURATION
        String successUrl = request.getSuccessUrl();
        String cancelUrl = request.getCancelUrl();
        
        // Set success URL with fallback chain
        if (successUrl == null || successUrl.isBlank()) {
            successUrl = stripeProperties.getSuccessUrl();
        }
        if (successUrl == null || successUrl.isBlank()) {
            successUrl = "http://localhost:3000/payment/success?session_id={CHECKOUT_SESSION_ID}";
        }
        
        // Set cancel URL with fallback chain
        if (cancelUrl == null || cancelUrl.isBlank()) {
            cancelUrl = stripeProperties.getCancelUrl();
        }
        if (cancelUrl == null || cancelUrl.isBlank()) {
            cancelUrl = "http://localhost:3000/payment/cancel";
        }

        // 4. STRIPE SESSION CREATION
        try {
            // Initialize Stripe session parameters
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putMetadata(ORDER_ID_KEY, String.valueOf(orderId))
                    .putMetadata(PAYMENT_ID_KEY, String.valueOf(paymentId));

            // 6. PRODUCT LINE ITEMS CREATION
            for (Object[] row : rows) {
                String title = (String) row[6];
                String color = (String) row[7];
                String size = (String) row[8];
                Long quantity = ((Number) row[9]).longValue();
                BigDecimal unitPrice = (BigDecimal) row[10];
                Long detailId = (Long) row[11];

                // Build product name with variants (color, size)
                String name = title;
                if (color != null) name += " - " + color;
                if (size != null) name += " - " + size;

                // Create product data for Stripe
                SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData
                        .builder().setName(name).build();

                var applyReq = PromotionApplyRequest.builder()
                .skuId(detailId)
                .basePrice(unitPrice)
                .build();
                PromotionApplyResponse applyRes = promotionService.applyPromotionForSku(applyReq);
        
                // Create price data for the line item
                SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(currency.toLowerCase())
                        .setUnitAmount(applyRes.getFinalPrice().movePointRight(0).longValue())
                        .setProductData(productData)
                        .build();

                // Create and add line item to session
                SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                        .setQuantity(quantity)
                        .setPriceData(priceData)
                        .build();

                paramsBuilder.addLineItem(lineItem);
            }

            // 7. SHIPPING FEE LINE ITEM
            // Add shipping fee as a separate line item if it exists
            if (shippingFee != null && shippingFee.compareTo(BigDecimal.ZERO) > 0) {
                SessionCreateParams.LineItem.PriceData.ProductData shippingProductData = SessionCreateParams.LineItem.PriceData.ProductData
                        .builder().setName("Shipping Fee").build();

                long shippingAmount = shippingFee.movePointRight(0).longValue();

                SessionCreateParams.LineItem.PriceData shippingPriceData = SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(currency.toLowerCase())
                        .setUnitAmount(shippingAmount)
                        .setProductData(shippingProductData)
                        .build();

                SessionCreateParams.LineItem shippingLineItem = SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(shippingPriceData)
                        .build();

                paramsBuilder.addLineItem(shippingLineItem);
            }

            // 8. DISCOUNT HANDLING
            if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                try {
                    // Create a coupon for the discount amount using StripeHelper
                    long discountAmountCents = stripeHelper.convertToCents(discountAmount);
                    String couponId = stripeHelper.createDiscountCoupon(discountAmountCents, currency);
                    
                    if (couponId != null) {
                        // Apply discount to the session
                        SessionCreateParams.Discount discount = SessionCreateParams.Discount.builder()
                                .setCoupon(couponId)
                                .build();
                        
                        paramsBuilder.addDiscount(discount);
                        log.info("Applied discount coupon: {} for amount: {}", couponId, stripeHelper.formatCurrency(discountAmount));
                    } else {
                        // Fallback: just add to metadata
                        paramsBuilder.putMetadata("discountAmount", discountAmount.toString());
                        log.warn("Failed to create coupon, added discount to metadata: {}", discountAmount);
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to create Stripe coupon for discount: {}", e.getMessage(), e);
                    // Fallback: just add to metadata
                    paramsBuilder.putMetadata("discountAmount", discountAmount.toString());
                }
            }
            
            // 9. METADATA & SESSION FINALIZATION
            if (shippingFee != null && shippingFee.compareTo(BigDecimal.ZERO) > 0) {
                paramsBuilder.putMetadata("shippingFee", shippingFee.toString());
            }
            paramsBuilder.putMetadata("totalAmount", totalAmount.toString());

            // Create the Stripe checkout session
            Session session = Session.create(paramsBuilder.build());

            // 10. PAYMENT ENTITY UPDATE
            payment.setProvider("STRIPE");
            payment.setTransactionNo(session.getId());
            payment.setStatus(PaymentStatus.UNPAID);
            paymentRepository.save(payment);

            // 11. RESPONSE CREATION
            return CheckoutSessionResponse.builder()
                    .sessionId(session.getId())
                    .checkoutUrl(session.getUrl())
                    .build();
                    
        } catch (StripeException e) {
            log.error("Inside PaymentServiceImpl.createCheckoutSessionFromContext, Failed to create Stripe Checkout session: {}", e.getMessage(), e);
            throw new ErrorException(HttpStatus.BAD_GATEWAY, "Stripe error: " + e.getMessage());
        }
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

    @Override 
    public RefundResponse refundPayment(RefundRequest request) {

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Payment not found with id: " + request.getPaymentId()));

        if (payment.getTransactionNo() == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Payment has no Stripe session/transaction to refund");
        }

        try {
            // Retrieve Checkout Session to get the payment_intent id
            Session session = Session.retrieve(payment.getTransactionNo());
            String paymentIntentId = session.getPaymentIntent();
            if (paymentIntentId == null || paymentIntentId.isBlank()) {
                throw new ErrorException(HttpStatus.BAD_REQUEST, "Stripe payment_intent not found for this session");
            }

            RefundCreateParams.Builder refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId);

            // Optional partial refund amount
            if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                long amountMinor = stripeHelper.convertToCents(request.getAmount());
                refundParams.setAmount(amountMinor);
            }

            // Optional reason
            if (request.getReason() != null && !request.getReason().isBlank()) {
                try {
                    refundParams.setReason(RefundCreateParams.Reason.valueOf(request.getReason().toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                    log.error("Inside PaymentServiceImpl.refundPayment, Invalid reason: {}", request.getReason());
                }
            }

            Refund refund = Refund.create(refundParams.build());

            // Update status of payment and order
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            Order order = payment.getOrder();
            if (order != null) {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
                orderRepository.save(order);
            }

            // Determine refunded amount and currency from Stripe refund response
            BigDecimal refundedAmount = request.getAmount() != null
                    ? request.getAmount()
                    : (refund.getAmount() != null ? new BigDecimal(refund.getAmount()) : null);
            String currency = refund.getCurrency();

            return RefundResponse.builder()
                    .refundId(refund.getId())
                    .status(refund.getStatus())
                    .amount(refundedAmount)
                    .currency(currency)
                    .build();
        } catch (StripeException e) {
            log.error("Inside PaymentServiceImpl.refundPayment, Failed to create Stripe refund for payment {}: {}", request.getPaymentId(), e.getMessage(), e);
            throw new ErrorException(HttpStatus.BAD_GATEWAY, "Stripe refund error: " + e.getMessage());
        }
    }

    private void handlePaymentSucceeded(Long orderId, String provider, String transactionNo) throws ErrorException {
        log.info("Inside PaymentServiceImpl.handlePaymentSucceeded, Order ID: {}", orderId);
        
        try {
            Order order = orderRepository.findByIdWithVoucher(orderId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
            
            // Check if this order was previously cancelled and needs stock deduction
            if (order.getPaymentStatus() == PaymentStatus.CANCELLED) {
                log.info("Order {} was previously cancelled, checking and deducting stock again", orderId);
                
                // Check if there's enough stock before deducting
                if (!inventoryService.hasEnoughStockForOrder(order)) {
                    log.error("Insufficient stock for previously cancelled order {}", orderId);
                    throw new ErrorException(HttpStatus.BAD_REQUEST, 
                            "Insufficient stock available for this order. Some products may have been sold out.");
                }
                
                // Deduct stock for previously cancelled order
                inventoryService.deductStockForOrder(order);
                log.info("Successfully deducted stock for previously cancelled order {}", orderId);
            }

            // Create OrderDetailPromotion records for order details with promotionId
            orderDetailPromotionService.createPromotionsForOrder(order);

            // Set payment status to PAID
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
            
            // Publish payment success event for user ranking
            eventPublisher.publishEvent(new PaymentSuccessEvent(
                    order.getUser().getId(), 
                    orderId, 
                    order.getTotalAmount()));
            log.info("Published PaymentSuccessEvent for user: {}, order: {}, amount: {}", 
                    order.getUser().getId(), orderId, order.getTotalAmount());
            
            // Send order confirmation email to user (non-blocking)
            orderEmailService.sendOrderDetailsEmail(order);
            log.info("Inside PaymentServiceImpl.handlePaymentSucceeded, Successfully sent order confirmation email to user: {}", orderId);
        } catch (Exception e) {
            log.error("Inside PaymentServiceImpl.handlePaymentSucceeded, Failed to process payment success for order {}: {}", orderId, e.getMessage(), e);
            throw e; // Re-throw to ensure proper error handling
        }
    }

    private void handlePaymentFailed(Long orderId, String provider, String transactionNo, String reason) {
        log.info("Inside PaymentServiceImpl.handlePaymentFailed, Order ID: {}, Reason: {}", orderId, reason);
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
            
            // Only restore stock if payment status is not already CANCELLED
            // This prevents double restoration if multiple failed events are received
            if (order.getPaymentStatus() != PaymentStatus.CANCELLED) {
                log.info("Restoring stock for failed payment, order: {}", orderId);
                inventoryService.restoreStockForOrder(order);
                log.info("Successfully restored stock for order: {}", orderId);
            } else {
                log.info("Order {} already has CANCELLED status, skipping stock restoration", orderId);
            }
            
            // Update payment status
            order.setPaymentStatus(PaymentStatus.CANCELLED);
            Payment payment = paymentRepository.findFirstByOrder_IdOrderByIdDesc(orderId)
                    .orElse(Payment.builder().order(order).amount(order.getTotalAmount()).build());
            payment.setProvider(provider);
            payment.setTransactionNo(transactionNo);
            payment.setStatus(PaymentStatus.CANCELLED);
            
            // Cancel voucher usage if any
            voucherService.cancelVoucherUsageByOrderId(orderId);
            
            paymentRepository.save(payment);
            orderRepository.save(order);
            
            log.info("Successfully processed payment failure for order: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to process payment failure for order {}: {}", orderId, e.getMessage(), e);
            // Don't re-throw here to avoid breaking webhook processing
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        log.info("Processing checkout.session.completed event: {}", event.getId());
        
        Session session = stripeHelper.extractSession(event);
        if (session == null) {
            log.warn("Failed to extract session from event: {}", event.getId());
            return;
        }
        
        if (!stripeHelper.validateSessionMetadata(session)) {
            return;
        }
        
        Long orderId = stripeHelper.extractOrderIdFromSession(session);
        if (orderId == null) {
            return;
        }
        
        try {
            handlePaymentSucceeded(orderId, "STRIPE", session.getId());
            log.info("Inside PaymentServiceImpl.handleCheckoutSessionCompleted, Successfully processed checkout session completed for order: {}", orderId);
        } catch (ErrorException ex) {
            log.error("Inside PaymentServiceImpl.handleCheckoutSessionCompleted, Failed to process payment success for order {}: {}", orderId, ex.getMessage(), ex);
        }
    }

    private void handleCheckoutSessionExpired(Event event) {
        log.info("Inside PaymentServiceImpl.handleCheckoutSessionExpired, Processing checkout.session.expired event: {}", event.getId());
        
        Session session = stripeHelper.extractSession(event);
        if (session == null) {
            log.warn("Inside PaymentServiceImpl.handleCheckoutSessionExpired, Failed to extract session from expired event: {}", event.getId());
            return;
        }
        
        if (!stripeHelper.validateSessionMetadata(session)) {
            return;
        }
        
        Long orderId = stripeHelper.extractOrderIdFromSession(session);
        if (orderId == null) {
            return;
        }
        
        try {
            handlePaymentFailed(orderId, "STRIPE", session.getId(), "SESSION_EXPIRED");
            log.info("Inside PaymentServiceImpl.handleCheckoutSessionExpired, Processing payment failure for expired order: {}", orderId);
        } catch (Exception ex) {
            log.warn("Inside PaymentServiceImpl.handleCheckoutSessionExpired, Error processing expired session for order {}: {}", orderId, ex.getMessage());
        }
    }

    private void handleRefunded(Event event) {
        Session session = stripeHelper.extractSession(event);
        if (session == null) {
            return;
        }
        
        if (!stripeHelper.validateSessionMetadata(session)) {
            return;
        }
        
        Long orderId = stripeHelper.extractOrderIdFromSession(session);
        if (orderId == null) {
            return;
        }
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            Payment payment = paymentRepository.findFirstByOrder_IdOrderByIdDesc(orderId)
                    .orElse(Payment.builder().order(order).amount(order.getTotalAmount()).build());
            payment.setProvider("STRIPE");
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            orderRepository.save(order);
            log.info("Inside PaymentServiceImpl.handleRefunded, Successfully processed refund for order: {}", orderId);
        } catch (Exception ex) {
            log.warn("Inside PaymentServiceImpl.handleRefunded, Error processing refund for order {}: {}", orderId, ex.getMessage());
        }
    }

    private void createVoucherUsage(Order order) {
        try {
            Voucher voucher = order.getVoucher();
            log.info("Inside PaymentServiceImpl.createVoucherUsage, Order {} voucher: {}", order.getId(), voucher);
            if (voucher == null) {
                log.warn("Inside PaymentServiceImpl.createVoucherUsage, No voucher found for order {}", order.getId());
                return;
            }

            // Idempotency guard: ensure only one VoucherUsage per order
            if (voucherUsageRepository.findByOrderId(order.getId()).isPresent()) {
                log.info("Inside PaymentServiceImpl.createVoucherUsage, VoucherUsage already exists for order {}, skipping creation", order.getId());
                return;
            }

            // Calculate discount amount
            BigDecimal discountAmount = stripeHelper.calculateVoucherDiscount(voucher, order.getSubtotalAmount());

            // Create VoucherUsage
            var voucherUsage = VoucherUsage.builder()
                    .voucher(voucher)
                    .user(order.getUser())
                    .order(order)
                    .discountAmount(discountAmount)
                    .status(VoucherUsageStatus.APPLIED)
                    .build();

            voucherUsageRepository.save(voucherUsage);
            log.info("Inside PaymentServiceImpl.createVoucherUsage, VoucherUsage created for order {} with voucher code {}", order.getId(), voucher.getCode());
        } catch (Exception e) {
            log.error("Inside PaymentServiceImpl.createVoucherUsage, Failed to create VoucherUsage for order {} with voucher: {}", order.getId(), e.getMessage(), e);
        }
    }

}


