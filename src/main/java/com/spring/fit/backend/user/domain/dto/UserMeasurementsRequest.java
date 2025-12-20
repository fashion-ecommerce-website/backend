package com.spring.fit.backend.user.domain.dto;

import com.spring.fit.backend.user.domain.enums.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMeasurementsRequest {

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Height is required")
    @DecimalMin(value = "140.0", message = "Height must be at least 140cm")
    @DecimalMax(value = "220.0", message = "Height must be less than 220cm")
    private BigDecimal height;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "35.0", message = "Weight must be at least 35kg")
    @DecimalMax(value = "200.0", message = "Weight must be less than 200kg")
    private BigDecimal weight;

    @NotNull(message = "Chest measurement is required")
    @DecimalMin(value = "60.0", message = "Chest must be at least 60cm")
    @DecimalMax(value = "150.0", message = "Chest must be less than 150cm")
    private BigDecimal chest;

    @NotNull(message = "Waist measurement is required")
    @DecimalMin(value = "50.0", message = "Waist must be at least 50cm")
    @DecimalMax(value = "150.0", message = "Waist must be less than 150cm")
    private BigDecimal waist;

    @NotNull(message = "Hips measurement is required")
    @DecimalMin(value = "60.0", message = "Hips must be at least 60cm")
    @DecimalMax(value = "160.0", message = "Hips must be less than 160cm")
    private BigDecimal hips;

    private BellyShape bellyShape;
    private HipShape hipShape;
    private ChestShape chestShape; // Optional, primarily for male
    private FitPreference fitPreference;
    private Boolean hasReturnHistory;
}
