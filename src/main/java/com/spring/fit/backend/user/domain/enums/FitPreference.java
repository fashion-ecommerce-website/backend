package com.spring.fit.backend.user.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FitPreference {
    TIGHT("TIGHT"),
    COMFORTABLE("COMFORTABLE"),
    LOOSE("LOOSE");

    private final String value;

    FitPreference(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
