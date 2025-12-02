package com.spring.fit.backend.order.domain.dto.tracking;

import com.spring.fit.backend.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingResponse {
    private String trackingNumber;
    private OrderStatus status;
    private String currentLocation;
    private List<TrackingEventData> events;
}













