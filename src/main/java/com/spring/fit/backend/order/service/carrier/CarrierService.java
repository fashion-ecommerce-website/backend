package com.spring.fit.backend.order.service.carrier;

import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.domain.entity.Shipment;
import com.spring.fit.backend.order.domain.dto.tracking.TrackingResponse;

public interface CarrierService {

    String createShipment(Shipment shipment, Order order);

    TrackingResponse getTrackingStatus(String trackingNumber);

    boolean supports(String carrier);
}


