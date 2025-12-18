package com.spring.fit.backend.order.service.impl;

import com.spring.fit.backend.common.enums.OrderStatus;
import com.spring.fit.backend.common.enums.PaymentStatus;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.domain.entity.Shipment;
import com.spring.fit.backend.order.repository.OrderRepository;
import com.spring.fit.backend.order.repository.ShipmentRepository;
import com.spring.fit.backend.order.service.ShipmentService;
import com.spring.fit.backend.order.service.carrier.CarrierService;
import com.spring.fit.backend.order.service.carrier.CarrierServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final CarrierServiceFactory carrierServiceFactory;

    @Value("${carrier.default:GHN}")
    private String defaultCarrier;

    @Override
    public Shipment createShipmentForOrder(Order order, String carrier) {
        log.info("Creating shipment for order {} with carrier {}", order.getId(), carrier);

        // Check if shipment already exists
        if (!shipmentRepository.findByOrderId(order.getId()).isEmpty()) {
            log.warn("Shipment already exists for order {}", order.getId());
            return shipmentRepository.findByOrderId(order.getId()).getFirst();
        }

        // Get carrier service
        String carrierToUse = (carrier != null && !carrier.isBlank()) ? carrier : defaultCarrier;
        CarrierService carrierService = carrierServiceFactory.getService(carrierToUse);

        // Create shipment entity
        Shipment shipment = Shipment.builder()
            .order(order)
            .carrier(carrierToUse)
            .status(OrderStatus.PENDING)
            .build();

        // Save shipment first (carrier API might need shipment ID)
        Shipment savedShipment = shipmentRepository.save(shipment);

        try {
            // Call carrier API to create shipment and get tracking number
            String trackingNumber = carrierService.createShipment(savedShipment, order);
            
            if (trackingNumber != null && !trackingNumber.isBlank()) {
                savedShipment.setTrackingNo(trackingNumber);
                savedShipment = shipmentRepository.save(savedShipment);
                log.info("Shipment created successfully with tracking number: {}", trackingNumber);
            } else {
                log.warn("Carrier returned empty tracking number for shipment {}", savedShipment.getId());
            }
        } catch (Exception e) {
            log.error("Failed to create shipment with carrier for order {}: {}", order.getId(), e.getMessage());
            // Keep shipment record even if carrier API fails - can retry later
        }

        return savedShipment;
    }
}

