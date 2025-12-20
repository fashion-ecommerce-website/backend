package com.spring.fit.backend.user.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.user.domain.dto.UserMeasurementsRequest;
import com.spring.fit.backend.user.domain.dto.UserMeasurementsResponse;
import com.spring.fit.backend.user.domain.entity.UserMeasurements;
import com.spring.fit.backend.user.repository.UserMeasurementsRepository;
import com.spring.fit.backend.user.service.UserMeasurementsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserMeasurementsServiceImpl implements UserMeasurementsService {

    private final UserMeasurementsRepository measurementsRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserMeasurementsResponse saveOrUpdateMeasurements(Long userId, UserMeasurementsRequest request) {
        log.info("Saving measurements for user: {}", userId);

        // Verify user exists
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        // Calculate BMI
        BigDecimal bmi = calculateBMI(request.getHeight(), request.getWeight());

        // Find existing measurements or create new
        UserMeasurements measurements = measurementsRepository.findByUserId(userId)
                .orElse(UserMeasurements.builder()
                        .user(user)
                        .build());

        // Update measurements
        measurements.setGender(request.getGender());
        measurements.setHeight(request.getHeight());
        measurements.setWeight(request.getWeight());
        measurements.setChest(request.getChest());
        measurements.setWaist(request.getWaist());
        measurements.setHips(request.getHips());
        measurements.setBmi(bmi);
        measurements.setBellyShape(request.getBellyShape());
        measurements.setHipShape(request.getHipShape());
        measurements.setChestShape(request.getChestShape());
        measurements.setFitPreference(request.getFitPreference());
        measurements.setHasReturnHistory(request.getHasReturnHistory() != null ? request.getHasReturnHistory() : false);

        UserMeasurements saved = measurementsRepository.save(measurements);
        log.info("Successfully saved measurements for user: {}, BMI: {}", userId, bmi);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserMeasurementsResponse> getMeasurementsByUserId(Long userId) {
        return measurementsRepository.findByUserId(userId)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteMeasurements(Long userId) {
        log.info("Deleting measurements for user: {}", userId);
        measurementsRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMeasurements(Long userId) {
        return measurementsRepository.existsByUserId(userId);
    }

    /**
     * Calculate BMI from height (cm) and weight (kg)
     * Formula: BMI = weight / (height in meters)^2
     */
    private BigDecimal calculateBMI(BigDecimal heightCm, BigDecimal weightKg) {
        // Convert height from cm to meters
        BigDecimal heightM = heightCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // Calculate height squared
        BigDecimal heightSquared = heightM.multiply(heightM);

        // Calculate BMI
        BigDecimal bmi = weightKg.divide(heightSquared, 2, RoundingMode.HALF_UP);

        return bmi;
    }

    /**
     * Map entity to response DTO
     */
    private UserMeasurementsResponse mapToResponse(UserMeasurements measurements) {
        return UserMeasurementsResponse.builder()
                .id(measurements.getId())
                .userId(measurements.getUser().getId())
                .gender(measurements.getGender())
                .age(measurements.getAge())
                .height(measurements.getHeight())
                .weight(measurements.getWeight())
                .chest(measurements.getChest())
                .waist(measurements.getWaist())
                .hips(measurements.getHips())
                .bmi(measurements.getBmi())
                .bellyShape(measurements.getBellyShape())
                .hipShape(measurements.getHipShape())
                .chestShape(measurements.getChestShape())
                .fitPreference(measurements.getFitPreference())
                .hasReturnHistory(measurements.getHasReturnHistory())
                .createdAt(measurements.getCreatedAt())
                .updatedAt(measurements.getUpdatedAt())
                .build();
    }
}
