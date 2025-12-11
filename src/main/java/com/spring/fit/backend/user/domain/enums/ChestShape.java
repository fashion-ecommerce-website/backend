package com.spring.fit.backend.user.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChestShape {
    SLIM("SLIM"),
    NORMAL("NORMAL"),
    BROAD("BROAD");

    private final String value;

    ChestShape(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
