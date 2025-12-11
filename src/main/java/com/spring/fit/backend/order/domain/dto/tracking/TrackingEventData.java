package com.spring.fit.backend.order.domain.dto.tracking;

import com.spring.fit.backend.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventData {
    private OrderStatus status;
    private String location;
    private String description;
    private LocalDateTime eventTime;
}













