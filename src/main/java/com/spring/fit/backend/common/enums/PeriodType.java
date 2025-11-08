package com.spring.fit.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PeriodType {
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    YEAR("year");

    private final String value;

    PeriodType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static PeriodType fromString(String period) {
        if (period == null || period.trim().isEmpty()) {
            return WEEK; 
        }
        
        String normalizedPeriod = period.trim().toLowerCase();
        
        for (PeriodType periodType : PeriodType.values()) {
            if (periodType.value.equals(normalizedPeriod)) {
                return periodType;
            }
        }
        
        try {
            return PeriodType.valueOf(period.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return WEEK; 
        }
    }
}
