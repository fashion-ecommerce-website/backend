package com.spring.fit.backend.order.controller;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.domain.dto.tracking.TrackingEventResponse;
import com.spring.fit.backend.order.service.TrackingService;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
public class TrackingController {

    private final TrackingService trackingService;
    private final UserRepository userRepository;

    @GetMapping("/shipments/{shipmentId}/events")
    public ResponseEntity<List<TrackingEventResponse>> getTrackingHistory(@PathVariable Long shipmentId) {
        UserEntity currentUser = getCurrentUser();
        log.info("Fetching tracking history for shipment {} by user {}", shipmentId, currentUser.getEmail());
        List<TrackingEventResponse> events = trackingService.getTrackingHistory(shipmentId, currentUser.getId());
        return ResponseEntity.ok(events);
    }

    @PostMapping("/shipments/{shipmentId}/refresh")
    public ResponseEntity<Void> refreshTracking(@PathVariable Long shipmentId) {
        UserEntity currentUser = getCurrentUser();
        log.info("Refreshing tracking for shipment {} by user {}", shipmentId, currentUser.getEmail());
        trackingService.refreshTracking(shipmentId, currentUser.getId());
        return ResponseEntity.accepted().build();
    }

    private UserEntity getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
    }
}













