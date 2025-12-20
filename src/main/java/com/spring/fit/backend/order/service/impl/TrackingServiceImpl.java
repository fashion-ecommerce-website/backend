package com.spring.fit.backend.order.service.impl;

import com.spring.fit.backend.common.enums.FulfillmentStatus;
import com.spring.fit.backend.common.enums.OrderStatus;
import com.spring.fit.backend.common.enums.PaymentMethod;
import com.spring.fit.backend.common.enums.PaymentStatus;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.domain.dto.tracking.TrackingEventData;
import com.spring.fit.backend.order.domain.dto.tracking.TrackingEventResponse;
import com.spring.fit.backend.order.domain.dto.tracking.TrackingResponse;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.domain.entity.Shipment;
import com.spring.fit.backend.order.domain.entity.TrackingEvent;
import com.spring.fit.backend.order.repository.OrderRepository;
import com.spring.fit.backend.order.repository.ShipmentRepository;
import com.spring.fit.backend.order.repository.TrackingEventRepository;
import com.spring.fit.backend.order.service.TrackingService;
import com.spring.fit.backend.order.service.carrier.CarrierService;
import com.spring.fit.backend.order.service.carrier.CarrierServiceFactory;
import com.spring.fit.backend.payment.domain.entity.Payment;
import com.spring.fit.backend.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TrackingServiceImpl implements TrackingService {

    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final CarrierServiceFactory carrierServiceFactory;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TrackingEventResponse> getTrackingHistory(Long shipmentId, Long userId) {
        Shipment shipment = getUserShipment(shipmentId, userId);
        List<TrackingEvent> events = trackingEventRepository.findByShipmentIdOrderByEventTimeDesc(shipment.getId());

        return events.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void refreshTracking(Long shipmentId, Long userId) {
        Shipment shipment = getUserShipment(shipmentId, userId);
        refreshTracking(shipment);
    }

    @Override
    public void refreshTracking(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ErrorException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found with id: " + shipmentId));
        refreshTracking(shipment);
    }

    private void refreshTracking(Shipment shipment) {
        if (shipment.getTrackingNo() == null || shipment.getTrackingNo().isBlank()) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Shipment does not have a tracking number yet");
        }

        CarrierService carrierService = carrierServiceFactory.getService(shipment.getCarrier());
        TrackingResponse trackingResponse = carrierService.getTrackingStatus(shipment.getTrackingNo());

        if (trackingResponse == null) {
            log.warn("Carrier returned null tracking response for shipment {}", shipment.getId());
            return;
        }

        updateShipmentStatus(shipment, trackingResponse);
        persistTrackingEvents(shipment, trackingResponse.getEvents());
    }

    private void updateShipmentStatus(Shipment shipment, TrackingResponse trackingResponse) {
        OrderStatus newStatus = trackingResponse.getStatus();
        if (newStatus == null) {
            return;
        }

        boolean statusChanged = newStatus != shipment.getStatus();
        if (!statusChanged) {
            return;
        }

        log.info("Updating shipment {} status from {} to {}", shipment.getId(), shipment.getStatus(), newStatus);
        shipment.setStatus(newStatus);

        if (newStatus == OrderStatus.SHIPPED && shipment.getShippedAt() == null) {
            shipment.setShippedAt(LocalDateTime.now());
        }

        if (newStatus == OrderStatus.DELIVERED && shipment.getDeliveredAt() == null) {
            shipment.setDeliveredAt(LocalDateTime.now());
            
            // Update order fulfillment status
            Order order = shipment.getOrder();
            order.setStatus(FulfillmentStatus.FULFILLED);
            
            // Handle COD payment - update payment status from UNPAID to PAID when delivered
            handleCODPaymentOnDelivery(order);
            
            orderRepository.save(order);
        }

        shipmentRepository.save(shipment);
    }

    private void handleCODPaymentOnDelivery(Order order) {
        try {
            // Check if order has COD payment method and is still UNPAID
            if (order.getPaymentStatus() == PaymentStatus.UNPAID) {
                // Get the payment for this order
                Payment payment = paymentRepository.findFirstByOrder_IdOrderByIdDesc(order.getId())
                        .orElse(null);
                
                if (payment != null && payment.getMethod() == PaymentMethod.CASH_ON_DELIVERY) {
                    log.info("Processing COD payment for delivered order: {}", order.getId());
                    
                    // Update payment status to PAID
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                    
                    // Update order payment status
                    order.setPaymentStatus(PaymentStatus.PAID);
                    
                    log.info("Successfully updated COD payment status to PAID for order: {}", order.getId());
                } else if (payment != null) {
                    log.debug("Order {} has payment method {} - not COD, skipping payment update", 
                            order.getId(), payment.getMethod());
                } else {
                    log.warn("No payment found for order: {}", order.getId());
                }
            } else {
                log.debug("Order {} already has payment status: {} - skipping payment update", 
                        order.getId(), order.getPaymentStatus());
            }
        } catch (Exception e) {
            log.error("Failed to process COD payment for order {}: {}", order.getId(), e.getMessage(), e);
        }
    }

    private void persistTrackingEvents(Shipment shipment, List<TrackingEventData> events) {
        if (CollectionUtils.isEmpty(events)) {
            return;
        }

        for (TrackingEventData eventData : events) {
            if (eventData.getEventTime() == null || eventData.getStatus() == null) {
                continue;
            }

            boolean exists = trackingEventRepository.existsByShipmentIdAndStatusAndEventTime(
                    shipment.getId(),
                    eventData.getStatus(),
                    eventData.getEventTime());

            if (exists) {
                continue;
            }

            TrackingEvent event = TrackingEvent.builder()
                    .shipment(shipment)
                    .status(eventData.getStatus())
                    .location(eventData.getLocation())
                    .description(eventData.getDescription())
                    .eventTime(eventData.getEventTime())
                    .build();

            trackingEventRepository.save(event);
        }
    }

    private Shipment getUserShipment(Long shipmentId, Long userId) {
        return shipmentRepository.findByIdAndOrderUserId(shipmentId, userId)
                .orElseThrow(() -> new ErrorException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found for current user"));
    }

    private TrackingEventResponse mapToResponse(TrackingEvent trackingEvent) {
        return TrackingEventResponse.builder()
                .id(trackingEvent.getId())
                .status(trackingEvent.getStatus() != null ? trackingEvent.getStatus().name() : null)
                .location(trackingEvent.getLocation())
                .description(trackingEvent.getDescription())
                .eventTime(trackingEvent.getEventTime())
                .build();
    }
}


