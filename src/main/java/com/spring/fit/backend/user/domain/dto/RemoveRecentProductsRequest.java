package com.spring.fit.backend.user.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveRecentProductsRequest {
    
    @NotNull(message = "List of product ID cannot be null")
    @NotEmpty(message = "List of product ID cannot be empty")
    private List<@Positive(message = "Product ID must be positive") Long> productIds;
}

