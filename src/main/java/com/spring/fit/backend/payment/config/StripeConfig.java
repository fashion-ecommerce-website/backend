package com.spring.fit.backend.payment.config;

import org.springframework.context.annotation.Configuration;

import com.stripe.Stripe;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StripeConfig {

    private final StripeProperties stripeProperties;

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeProperties.getApiKey();
        if (Stripe.apiKey == null || Stripe.apiKey.isBlank()) {
            log.warn("Stripe API key is not configured");
        } else {
            log.info("Stripe API initialized");
        }
    }
}





