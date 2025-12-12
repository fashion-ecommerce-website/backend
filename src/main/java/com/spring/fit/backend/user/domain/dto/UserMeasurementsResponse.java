package com.spring.fit.backend.user.domain.dto;

import com.spring.fit.backend.user.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMeasurementsResponse {

    private Long id;
    private Long userId;
    private Gender gender;
    private Integer age;
    private BigDecimal height;
    private BigDecimal weight;
    private BigDecimal chest;
    private BigDecimal waist;
    private BigDecimal hips;
    private BigDecimal bmi;
    private BellyShape bellyShape;
    private HipShape hipShape;
    private ChestShape chestShape;
    private FitPreference fitPreference;
    private Boolean hasReturnHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
