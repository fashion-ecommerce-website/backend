package com.spring.fit.backend.recommendation.domain.enums;

public enum AgeGroup {
    AGE_18_25(18, 25),
    AGE_25_35(25, 35),
    AGE_35_45(35, 45),
    AGE_45_PLUS(45, 100);

    private final int min;
    private final int max;

    AgeGroup(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public static AgeGroup fromAge(int age) {
        if (age < 25) {
            return AGE_18_25;
        } else if (age < 35) {
            return AGE_25_35;
        } else if (age < 45) {
            return AGE_35_45;
        } else {
            return AGE_45_PLUS;
        }
    }
}
