package com.spring.fit.backend.order.repository;

import com.spring.fit.backend.common.enums.OrderStatus;
import com.spring.fit.backend.order.domain.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByIdAndOrderUserId(Long id, Long userId);

    Optional<Shipment> findByTrackingNo(String trackingNo);

    List<Shipment> findByStatusIn(Collection<OrderStatus> statuses);

    List<Shipment> findByOrderId(Long orderId);
}


