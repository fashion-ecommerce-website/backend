package com.spring.fit.backend.user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentSuccessEvent {
    private final Long userId;
    private final Long orderId;
    private final BigDecimal amount;
}