package com.spring.fit.backend.user.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HipShape {
    NARROW("NARROW"),
    NORMAL("NORMAL"),
    WIDE("WIDE");

    private final String value;

    HipShape(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
