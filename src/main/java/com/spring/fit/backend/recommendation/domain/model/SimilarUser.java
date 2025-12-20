package com.spring.fit.backend.recommendation.domain.model;

import com.spring.fit.backend.user.domain.entity.UserMeasurements;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarUser {
    private UserMeasurements measurements;
    private double similarity; // 0.0 to 1.0
    private double distance;   // Euclidean distance

    public Long getUserId() {
        return measurements.getUser().getId();
    }
}
