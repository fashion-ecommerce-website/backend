package com.spring.fit.backend.order.service;

import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.domain.entity.Shipment;

public interface ShipmentService {
    Shipment createShipmentForOrder(Order order, String carrier);
}
