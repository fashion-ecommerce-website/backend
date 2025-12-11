package com.spring.fit.backend.recommendation.util;

import com.spring.fit.backend.user.domain.entity.UserMeasurements;
import com.spring.fit.backend.user.domain.enums.FitPreference;

import java.math.BigDecimal;

/**
 * Utility class for calculating similarity between users based on their body measurements
 * and preferences. Uses a combination of Euclidean distance for continuous measurements
 * and categorical matching for discrete attributes.
 */
public class SimilarityCalculator {

    // ==================== WEIGHTS FOR CONTINUOUS MEASUREMENTS ====================
    // These are used in Euclidean distance calculation
    private static final double CHEST_WEIGHT = 1.0;
    private static final double WAIST_WEIGHT = 1.0;
    private static final double HIPS_WEIGHT = 1.0;
    private static final double HEIGHT_WEIGHT = 0.1;   // Less important for size
    private static final double WEIGHT_WEIGHT = 0.05;  // Already reflected in BMI

    // ==================== WEIGHTS FOR CATEGORICAL ATTRIBUTES ====================
    // These are used as multipliers for the final similarity score
    private static final double FIT_PREFERENCE_MATCH_BONUS = 0.15;      // +15% if same fit preference
    private static final double FIT_PREFERENCE_ADJACENT_BONUS = 0.05;   // +5% if adjacent (TIGHT-COMFORTABLE or COMFORTABLE-LOOSE)
    private static final double BELLY_SHAPE_MATCH_BONUS = 0.08;         // +8% if same belly shape
    private static final double HIP_SHAPE_MATCH_BONUS = 0.08;           // +8% if same hip shape
    private static final double CHEST_SHAPE_MATCH_BONUS = 0.08;         // +8% if same chest shape

    /**
     * Calculate Euclidean distance between two users' body measurements.
     * Lower distance = more similar body measurements.
     * 
     * Only considers continuous numerical measurements:
     * - Chest, Waist, Hips (primary - weight 1.0)
     * - Height, Weight (secondary - lower weights)
     */
    public static double calculateEuclideanDistance(
            UserMeasurements user1,
            UserMeasurements user2) {

        double chestDiff = toDouble(user1.getChest()) - toDouble(user2.getChest());
        double waistDiff = toDouble(user1.getWaist()) - toDouble(user2.getWaist());
        double hipsDiff = toDouble(user1.getHips()) - toDouble(user2.getHips());
        double heightDiff = toDouble(user1.getHeight()) - toDouble(user2.getHeight());
        double weightDiff = toDouble(user1.getWeight()) - toDouble(user2.getWeight());

        return Math.sqrt(
            Math.pow(chestDiff * CHEST_WEIGHT, 2) +
            Math.pow(waistDiff * WAIST_WEIGHT, 2) +
            Math.pow(hipsDiff * HIPS_WEIGHT, 2) +
            Math.pow(heightDiff * HEIGHT_WEIGHT, 2) +
            Math.pow(weightDiff * WEIGHT_WEIGHT, 2)
        );
    }

    /**
     * Convert Euclidean distance to base similarity score (0.0 to 1.0).
     * Higher similarity = more similar.
     */
    public static double distanceToSimilarity(double distance) {
        return 1.0 / (1.0 + distance);
    }

    /**
     * Calculate comprehensive similarity score including both body measurements
     * and categorical attributes (fit preference, body shapes).
     * 
     * Formula:
     * finalSimilarity = baseSimilarity × (1 + categoryBonuses)
     * 
     * Where categoryBonuses can add up to ~39% bonus for perfect matches:
     * - fitPreference match: +15%
     * - bellyShape match: +8%
     * - hipShape match: +8%
     * - chestShape match: +8%
     * 
     * @return Similarity score (can exceed 1.0 with bonuses, capped at 1.0)
     */
    public static double calculateSimilarity(
            UserMeasurements user1,
            UserMeasurements user2) {
        
        // Step 1: Calculate base similarity from body measurements
        double distance = calculateEuclideanDistance(user1, user2);
        double baseSimilarity = distanceToSimilarity(distance);
        
        // Step 2: Calculate categorical bonuses
        double categoryBonus = 0.0;
        
        // Fit Preference bonus
        categoryBonus += calculateFitPreferenceBonus(
            user1.getFitPreference(), 
            user2.getFitPreference()
        );
        
        // Belly Shape bonus
        categoryBonus += calculateShapeBonus(
            user1.getBellyShape(), 
            user2.getBellyShape(),
            BELLY_SHAPE_MATCH_BONUS
        );
        
        // Hip Shape bonus
        categoryBonus += calculateShapeBonus(
            user1.getHipShape(), 
            user2.getHipShape(),
            HIP_SHAPE_MATCH_BONUS
        );
        
        // Chest Shape bonus
        categoryBonus += calculateShapeBonus(
            user1.getChestShape(), 
            user2.getChestShape(),
            CHEST_SHAPE_MATCH_BONUS
        );
        
        // Step 3: Apply bonuses to base similarity
        double finalSimilarity = baseSimilarity * (1.0 + categoryBonus);
        
        // Cap at 1.0
        return Math.min(finalSimilarity, 1.0);
    }

    /**
     * Calculate bonus for fit preference matching.
     * - Exact match: +15%
     * - Adjacent (TIGHT-COMFORTABLE or COMFORTABLE-LOOSE): +5%
     * - Opposite (TIGHT-LOOSE): 0%
     */
    private static double calculateFitPreferenceBonus(FitPreference pref1, FitPreference pref2) {
        if (pref1 == null || pref2 == null) {
            return 0.0;
        }
        
        if (pref1 == pref2) {
            return FIT_PREFERENCE_MATCH_BONUS;
        }
        
        // Check if adjacent (TIGHT-COMFORTABLE or COMFORTABLE-LOOSE)
        if (pref1 == FitPreference.COMFORTABLE || pref2 == FitPreference.COMFORTABLE) {
            return FIT_PREFERENCE_ADJACENT_BONUS;
        }
        
        // TIGHT vs LOOSE - no bonus
        return 0.0;
    }

    /**
     * Calculate bonus for body shape matching (generic for belly, hip, chest shapes).
     * - Exact match: full bonus
     * - Different: no bonus
     */
    private static <T extends Enum<T>> double calculateShapeBonus(T shape1, T shape2, double matchBonus) {
        if (shape1 == null || shape2 == null) {
            return 0.0;
        }
        
        return shape1 == shape2 ? matchBonus : 0.0;
    }

    /**
     * Calculate similarity with detailed breakdown for debugging/logging.
     */
    public static SimilarityBreakdown calculateSimilarityWithBreakdown(
            UserMeasurements user1,
            UserMeasurements user2) {
        
        double distance = calculateEuclideanDistance(user1, user2);
        double baseSimilarity = distanceToSimilarity(distance);
        
        double fitPrefBonus = calculateFitPreferenceBonus(user1.getFitPreference(), user2.getFitPreference());
        double bellyBonus = calculateShapeBonus(user1.getBellyShape(), user2.getBellyShape(), BELLY_SHAPE_MATCH_BONUS);
        double hipBonus = calculateShapeBonus(user1.getHipShape(), user2.getHipShape(), HIP_SHAPE_MATCH_BONUS);
        double chestBonus = calculateShapeBonus(user1.getChestShape(), user2.getChestShape(), CHEST_SHAPE_MATCH_BONUS);
        
        double totalBonus = fitPrefBonus + bellyBonus + hipBonus + chestBonus;
        double finalSimilarity = Math.min(baseSimilarity * (1.0 + totalBonus), 1.0);
        
        return new SimilarityBreakdown(
            distance,
            baseSimilarity,
            fitPrefBonus,
            bellyBonus,
            hipBonus,
            chestBonus,
            totalBonus,
            finalSimilarity
        );
    }

    private static double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    /**
     * Data class for similarity calculation breakdown (useful for debugging/logging)
     */
    public static class SimilarityBreakdown {
        public final double euclideanDistance;
        public final double baseSimilarity;
        public final double fitPreferenceBonus;
        public final double bellyShapeBonus;
        public final double hipShapeBonus;
        public final double chestShapeBonus;
        public final double totalBonus;
        public final double finalSimilarity;

        public SimilarityBreakdown(
                double euclideanDistance,
                double baseSimilarity,
                double fitPreferenceBonus,
                double bellyShapeBonus,
                double hipShapeBonus,
                double chestShapeBonus,
                double totalBonus,
                double finalSimilarity) {
            this.euclideanDistance = euclideanDistance;
            this.baseSimilarity = baseSimilarity;
            this.fitPreferenceBonus = fitPreferenceBonus;
            this.bellyShapeBonus = bellyShapeBonus;
            this.hipShapeBonus = hipShapeBonus;
            this.chestShapeBonus = chestShapeBonus;
            this.totalBonus = totalBonus;
            this.finalSimilarity = finalSimilarity;
        }

        @Override
        public String toString() {
            return String.format(
                "SimilarityBreakdown{distance=%.2f, base=%.3f, fitPref=+%.1f%%, belly=+%.1f%%, hip=+%.1f%%, chest=+%.1f%%, total=+%.1f%%, final=%.3f}",
                euclideanDistance, baseSimilarity,
                fitPreferenceBonus * 100, bellyShapeBonus * 100, hipShapeBonus * 100, chestShapeBonus * 100,
                totalBonus * 100, finalSimilarity
            );
        }
    }
}
