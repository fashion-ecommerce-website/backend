package com.spring.fit.backend.order.service;

import com.spring.fit.backend.order.domain.dto.tracking.TrackingEventResponse;

import java.util.List;

public interface TrackingService {

    List<TrackingEventResponse> getTrackingHistory(Long shipmentId, Long userId);

    void refreshTracking(Long shipmentId, Long userId);

    void refreshTracking(Long shipmentId);
}


