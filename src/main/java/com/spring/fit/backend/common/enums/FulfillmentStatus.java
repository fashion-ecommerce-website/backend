package com.spring.fit.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FulfillmentStatus {
    UNFULFILLED("UNFULFILLED"),
    FULFILLED("FULFILLED"),
    PARTIALLY_FULFILLED("PARTIALLY_FULFILLED"),
    CANCELLED("CANCELLED");

    private final String value;

    FulfillmentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
