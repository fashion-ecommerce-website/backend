package com.spring.fit.backend.user.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BellyShape {
    FLAT("FLAT"),
    NORMAL("NORMAL"),
    ROUND("ROUND");

    private final String value;

    BellyShape(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
