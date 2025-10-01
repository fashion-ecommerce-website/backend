package com.spring.fit.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus {
    UNPAID("UNPAID"),
    PAID("PAID"),
    PARTIALLY_PAID("PARTIALLY_PAID"),
    REFUNDED("REFUNDED"),
    CANCELLED("CANCELLED");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
