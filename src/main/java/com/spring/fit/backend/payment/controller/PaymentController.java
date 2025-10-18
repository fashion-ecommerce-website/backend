package com.spring.fit.backend.payment.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.payment.config.StripeProperties;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.CreateCheckoutRequest;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.CheckoutSessionResponse;
import com.spring.fit.backend.payment.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
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
            log.warn("Inside PaymentController.stripeWebhook missing signature header");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Inside PaymentController.stripeWebhook invalid signature");
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Invalid signature");
        }

        // 2) Route event to service
        paymentService.handleStripeEvent(event);

        // 3) Acknowledge receipt
        return ResponseEntity.ok("Inside PaymentController.stripeWebhook received");
    }

    // Intentionally left without local handlers; all event handling is in the service

}


