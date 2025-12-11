package com.spring.fit.backend.recommendation.domain.enums;

import java.math.BigDecimal;

public enum BMIRange {
    BMI_15_20(new BigDecimal("15.0"), new BigDecimal("20.0")),
    BMI_20_25(new BigDecimal("20.0"), new BigDecimal("25.0")),
    BMI_25_30(new BigDecimal("25.0"), new BigDecimal("30.0")),
    BMI_30_40(new BigDecimal("30.0"), new BigDecimal("40.0"));

    private final BigDecimal min;
    private final BigDecimal max;

    BMIRange(BigDecimal min, BigDecimal max) {
        this.min = min;
        this.max = max;
    }

    public BigDecimal getMin() {
        return min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public static BMIRange fromBMI(BigDecimal bmi) {
        if (bmi.compareTo(new BigDecimal("20.0")) < 0) {
            return BMI_15_20;
        } else if (bmi.compareTo(new BigDecimal("25.0")) < 0) {
            return BMI_20_25;
        } else if (bmi.compareTo(new BigDecimal("30.0")) < 0) {
            return BMI_25_30;
        } else {
            return BMI_30_40;
        }
    }
}
