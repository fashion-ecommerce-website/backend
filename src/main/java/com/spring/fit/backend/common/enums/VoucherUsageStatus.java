package com.spring.fit.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VoucherUsageStatus {
    APPLIED("APPLIED"),
    CANCELLED("CANCELLED"),
    RESERVED("RESERVED");

    private final String value;

    VoucherUsageStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

