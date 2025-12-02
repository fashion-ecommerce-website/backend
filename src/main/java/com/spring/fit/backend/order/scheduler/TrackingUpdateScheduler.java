package com.spring.fit.backend.order.scheduler;

import com.spring.fit.backend.common.enums.OrderStatus;
import com.spring.fit.backend.order.domain.entity.Shipment;
import com.spring.fit.backend.order.repository.ShipmentRepository;
import com.spring.fit.backend.order.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrackingUpdateScheduler {

    private static final Set<OrderStatus> ACTIVE_STATUSES = EnumSet.of(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PROCESSING,
            OrderStatus.SHIPPED
    );

    private final ShipmentRepository shipmentRepository;
    private final TrackingService trackingService;

    @Scheduled(fixedDelayString = "${tracking.update-interval:300000}")
    public void refreshActiveShipments() {
        List<Shipment> shipments = shipmentRepository.findByStatusIn(ACTIVE_STATUSES);
        if (shipments.isEmpty()) {
            return;
        }

        for (Shipment shipment : shipments) {
            try {
                trackingService.refreshTracking(shipment.getId());
            } catch (Exception ex) {
                log.warn("Unable to refresh tracking for shipment {}: {}", shipment.getId(), ex.getMessage());
            }
        }
    }
}
