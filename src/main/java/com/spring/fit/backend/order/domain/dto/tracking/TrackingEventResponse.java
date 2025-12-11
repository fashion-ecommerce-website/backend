package com.spring.fit.backend.order.domain.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventResponse {
    private Long id;
    private String status;
    private String location;
    private String description;
    private LocalDateTime eventTime;
}













