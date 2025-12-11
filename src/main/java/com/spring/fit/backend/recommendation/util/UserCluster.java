package com.spring.fit.backend.recommendation.util;

import com.spring.fit.backend.user.domain.entity.UserMeasurements;
import com.spring.fit.backend.user.domain.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Helper class for clustering users by demographic and body measurements
 * Used to reduce candidate pool before similarity calculation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCluster {
    
    private Gender gender;          // MALE, FEMALE
    private String bmiRange;        // BMI_15_20, BMI_20_25, BMI_25_30, BMI_30_40
    private String ageGroup;        // AGE_18_25, AGE_25_35, AGE_35_45, AGE_45_PLUS
    
    /**
     * Create cluster from user measurements
     */
    public static UserCluster fromUser(UserMeasurements measurements) {
        return new UserCluster(
            measurements.getGender(),
            getBMIRange(measurements.getBmi()),
            getAgeGroup(measurements.getAge())
        );
    }
    
    /**
     * Determine BMI range category
     */
    private static String getBMIRange(BigDecimal bmi) {
        double bmiValue = bmi.doubleValue();
        if (bmiValue < 20) return "BMI_15_20";
        if (bmiValue < 25) return "BMI_20_25";
        if (bmiValue < 30) return "BMI_25_30";
        return "BMI_30_40";
    }
    
    /**
     * Determine age group category
     */
    private static String getAgeGroup(Integer age) {
        if (age < 25) return "AGE_18_25";
        if (age < 35) return "AGE_25_35";
        if (age < 45) return "AGE_35_45";
        return "AGE_45_PLUS";
    }
    
    /**
     * Get cluster bounds for database query
     */
    public ClusterBounds getBounds() {
        // BMI bounds (±2.5 from range center)
        double[] bmiBounds = switch (bmiRange) {
            case "BMI_15_20" -> new double[]{15.0, 22.5};
            case "BMI_20_25" -> new double[]{17.5, 27.5};
            case "BMI_25_30" -> new double[]{22.5, 32.5};
            case "BMI_30_40" -> new double[]{27.5, 40.0};
            default -> new double[]{15.0, 40.0};
        };
        
        // Age bounds (±5 years from range center)
        int[] ageBounds = switch (ageGroup) {
            case "AGE_18_25" -> new int[]{18, 30};
            case "AGE_25_35" -> new int[]{20, 40};
            case "AGE_35_45" -> new int[]{30, 50};
            case "AGE_45_PLUS" -> new int[]{40, 100};
            default -> new int[]{18, 100};
        };
        
        return new ClusterBounds(
            BigDecimal.valueOf(bmiBounds[0]),
            BigDecimal.valueOf(bmiBounds[1]),
            ageBounds[0],
            ageBounds[1]
        );
    }
    
    /**
     * Cluster bounds for database filtering
     */
    @Data
    @AllArgsConstructor
    public static class ClusterBounds {
        private BigDecimal bmiMin;
        private BigDecimal bmiMax;
        private Integer ageMin;
        private Integer ageMax;
    }
}
