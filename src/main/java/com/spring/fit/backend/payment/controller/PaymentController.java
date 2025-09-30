package com.spring.fit.backend.payment.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.fit.backend.payment.config.StripeProperties;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.CreateCheckoutRequest;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.CheckoutSessionResponse;
import com.spring.fit.backend.payment.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeProperties stripeProperties;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutSessionResponse> createCheckout(
            @RequestBody CreateCheckoutRequest request) {
        CheckoutSessionResponse response = paymentService.createCheckoutSessionFromContext(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> stripeWebhook(@RequestBody String payload,
            @RequestHeader(name = "Stripe-Signature", required = false) String signatureHeader,
            HttpServletRequest request) {
        // 1) Validate signature
        if (signatureHeader == null) {
            return badRequest("Missing signature header");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Webhook signature verification failed", e);
            return badRequest("Invalid signature");
        }

        // 2) Route event
        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }

        // 3) Acknowledge receipt
        return ResponseEntity.ok("received");
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = extractSession(event);
        if (session == null || session.getMetadata() == null) {
            log.warn("checkout.session.completed without session/metadata");
            return;
        }
        String orderIdStr = session.getMetadata().get("orderId");
        if (orderIdStr == null) {
            log.warn("checkout.session.completed missing orderId metadata");
            return;
        }
        try {
            Long orderId = Long.parseLong(orderIdStr);
            paymentService.handlePaymentSucceeded(orderId, "STRIPE", session.getId());
        } catch (NumberFormatException ex) {
            log.warn("Invalid orderId in metadata: {}", orderIdStr);
        }
    }

    private Session extractSession(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        return (Session) deserializer.getObject().orElse(null);
    }

    private ResponseEntity<String> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}


