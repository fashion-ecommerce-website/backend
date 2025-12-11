package com.spring.fit.backend.order.repository;

import com.spring.fit.backend.common.enums.OrderStatus;
import com.spring.fit.backend.order.domain.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByShipmentIdOrderByEventTimeDesc(Long shipmentId);

    boolean existsByShipmentIdAndStatusAndEventTime(Long shipmentId, OrderStatus status, LocalDateTime eventTime);
}
