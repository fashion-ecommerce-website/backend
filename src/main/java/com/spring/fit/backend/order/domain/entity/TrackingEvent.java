package com.spring.fit.backend.order.domain.entity;

import com.spring.fit.backend.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tracking_events", indexes = {
        @Index(name = "idx_tracking_event_shipment_id", columnList = "shipment_id"),
        @Index(name = "idx_tracking_event_event_time", columnList = "event_time")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderStatus status;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TrackingEvent that = (TrackingEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}













