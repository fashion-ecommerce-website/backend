package com.spring.fit.backend.user.controller;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.user.domain.dto.UserMeasurementsRequest;
import com.spring.fit.backend.user.domain.dto.UserMeasurementsResponse;
import com.spring.fit.backend.user.service.UserMeasurementsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/measurements")
@RequiredArgsConstructor
@Slf4j
public class UserMeasurementsController {

    private final UserMeasurementsService measurementsService;
    private final UserRepository userRepository;

    /**
     * Save or update measurements for current authenticated user
     * POST /api/users/measurements
     */
    @PostMapping
    public ResponseEntity<UserMeasurementsResponse> saveOrUpdateMeasurements(
            @Valid @RequestBody UserMeasurementsRequest request) {
        Long userId = getCurrentUserId();
        log.info("Received measurements from user: {}", userId);

        UserMeasurementsResponse response = measurementsService.saveOrUpdateMeasurements(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get measurements for current authenticated user
     * GET /api/users/measurements
     */
    @GetMapping
    public ResponseEntity<UserMeasurementsResponse> getMeasurements() {
        Long userId = getCurrentUserId();

        return measurementsService.getMeasurementsByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete measurements for current authenticated user
     * DELETE /api/users/measurements
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteMeasurements() {
        Long userId = getCurrentUserId();
        log.info("Deleting measurements for user: {}", userId);

        measurementsService.deleteMeasurements(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if current user has measurements
     * GET /api/users/measurements/exists
     */
    @GetMapping("/exists")
    public ResponseEntity<Boolean> hasMeasurements() {
        Long userId = getCurrentUserId();
        boolean exists = measurementsService.hasMeasurements(userId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null ||
                    "anonymousUser".equals(authentication.getName())) {
                throw new ErrorException(HttpStatus.UNAUTHORIZED, "User must be authenticated");
            }

            String email = authentication.getName();
            UserEntity user = userRepository.findActiveUserByEmail(email)
                    .orElseThrow(() -> new ErrorException(HttpStatus.UNAUTHORIZED, "User not found"));

            return user.getId();
        } catch (ErrorException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting current user ID: {}", e.getMessage(), e);
            throw new ErrorException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
    }
}
