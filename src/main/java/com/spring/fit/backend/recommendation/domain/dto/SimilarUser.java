package com.spring.fit.backend.recommendation.domain.dto;

import com.spring.fit.backend.user.domain.entity.UserMeasurements;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Wrapper class for user measurements with similarity metrics
 */
@Data
@AllArgsConstructor
public class SimilarUser {
    private UserMeasurements measurements;
    private double distance;      // Euclidean distance from target user
    private double similarity;    // Similarity score (0-1)
}
