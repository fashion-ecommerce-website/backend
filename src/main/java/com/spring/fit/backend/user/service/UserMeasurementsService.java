package com.spring.fit.backend.user.service;

import com.spring.fit.backend.user.domain.dto.UserMeasurementsRequest;
import com.spring.fit.backend.user.domain.dto.UserMeasurementsResponse;

import java.util.Optional;

public interface UserMeasurementsService {

    /**
     * Save or update user measurements
     * BMI will be calculated automatically
     */
    UserMeasurementsResponse saveOrUpdateMeasurements(Long userId, UserMeasurementsRequest request);

    /**
     * Get measurements by user ID
     */
    Optional<UserMeasurementsResponse> getMeasurementsByUserId(Long userId);

    /**
     * Delete measurements for a user
     */
    void deleteMeasurements(Long userId);

    /**
     * Check if user has measurements
     */
    boolean hasMeasurements(Long userId);
}
