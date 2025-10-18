package com.spring.fit.backend.payment.service;

import com.spring.fit.backend.payment.domain.dto.PaymentDtos.CreateCheckoutRequest;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.CheckoutSessionResponse;
import com.stripe.model.Event;

public interface PaymentService {

    CheckoutSessionResponse createCheckoutSessionFromContext(CreateCheckoutRequest request);

    void handlePaymentSucceeded(Long orderId, String provider, String transactionNo);

    void handlePaymentFailed(Long orderId, String provider, String transactionNo, String reason);

    void handleStripeEvent(Event event);
}


