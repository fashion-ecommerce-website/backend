package com.spring.fit.backend.recommendation.domain.enums;

import java.math.BigDecimal;

/**
 * BMI Range categories for user clustering.
 * 
 * Each range has OVERLAP with adjacent ranges (±2.5 BMI) to:
 * 1. Find more similar users at boundary values
 * 2. Avoid hard cutoffs that miss relevant matches
 * 
 * Example: User with BMI=20 falls into BMI_18_22 category,
 * which queries users with BMI 15.5-24.5 (overlapping with adjacent ranges)
 */
public enum BMIRange {
    // Underweight to normal: center=18, query range 15.0-22.5
    BMI_UNDER_20(new BigDecimal("15.0"), new BigDecimal("22.5")),
    // Normal weight: center=22.5, query range 17.5-27.5
    BMI_20_25(new BigDecimal("17.5"), new BigDecimal("27.5")),
    // Overweight: center=27.5, query range 22.5-32.5
    BMI_25_30(new BigDecimal("22.5"), new BigDecimal("32.5")),
    // Obese: center=32.5, query range 27.5-40.0
    BMI_OVER_30(new BigDecimal("27.5"), new BigDecimal("40.0"));

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

    /**
     * Determine BMI range category from exact BMI value.
     * Uses standard WHO BMI classification boundaries.
     */
    public static BMIRange fromBMI(BigDecimal bmi) {
        double bmiValue = bmi.doubleValue();
        if (bmiValue < 20.0) {
            return BMI_UNDER_20;
        } else if (bmiValue < 25.0) {
            return BMI_20_25;
        } else if (bmiValue < 30.0) {
            return BMI_25_30;
        } else {
            return BMI_OVER_30;
        }
    }
}
