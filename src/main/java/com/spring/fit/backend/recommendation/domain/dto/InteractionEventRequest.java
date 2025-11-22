package com.spring.fit.backend.recommendation.domain.dto;

import com.spring.fit.backend.recommendation.domain.enums.ActionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionEventRequest {
    @NotNull(message = "ProductId is required")
    private Long productId;
    
    @Positive(message = "Count must be positive")
    private Integer count = 1;
    
    @NotNull(message = "ActionType is required")
    private ActionType actionType; // VIEW, LIKE, ADD_TO_CART, PURCHASE
}
