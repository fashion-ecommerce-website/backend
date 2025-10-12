package com.spring.fit.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VoucherType {
    PERCENT("PERCENT"),
    FIXED("FIXED");

    private final String value;

    VoucherType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

