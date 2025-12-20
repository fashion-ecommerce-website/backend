package com.spring.fit.backend.recommendation.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeRecommendationResponse {

    private String recommendedSize; // e.g., "M"
    private Double confidence; // 0.0 - 1.0
    private List<AlternativeSize> alternatives;
    private RecommendationMetadata metadata; // NEW: Structured metadata
    private Boolean hasMeasurements; // Does user have measurements saved?

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeSize {
        private String size; // e.g., "L"
        private Double confidence; // e.g., 0.35
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationMetadata {
        private Integer totalSimilarUsers; // Number of similar users found
        private Integer totalPurchases; // Total purchases from similar users
        private Double averageRating; // Average review rating (0.0-5.0)
        private Double highRatingRatio; // % of purchases with rating >= 4.0
        private ConfidenceLevel confidenceLevel; // HIGH, MEDIUM, LOW
        private DataQuality dataQuality; // EXCELLENT, GOOD, FAIR, LIMITED
        private Boolean hasCloseAlternative; // Is there a close alternative?
    }

    public enum ConfidenceLevel {
        HIGH, // >= 0.70
        MEDIUM, // 0.50 - 0.69
        LOW // < 0.50
    }

    public enum DataQuality {
        EXCELLENT, // >= 20 users, high ratings
        GOOD, // 10-19 users, good ratings
        FAIR, // 5-9 users
        LIMITED // < 5 users
    }
}
