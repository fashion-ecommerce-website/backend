package com.spring.fit.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AudienceType {
    ALL("ALL"),
    RANK("RANK");

    private final String value;

    AudienceType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

