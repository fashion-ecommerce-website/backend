package com.spring.fit.backend.cart.domain.dto.request;

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
public class RemoveFromCartRequest {

    @NotEmpty(message = "Cart detail ID cannot be null")
    private List<@Positive(message = "Cart detail ID must be positive") Long> cartDetailIds;
}

