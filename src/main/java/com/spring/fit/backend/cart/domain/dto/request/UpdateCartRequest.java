package com.spring.fit.backend.cart.domain.dto.request;

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
public class UpdateCartRequest {

    @NotNull(message = "Cart detail ID cannot be null")
    @Positive(message = "Cart detail ID must be positive")
    private Long cartDetailId;

    @NotNull(message = "Product detail ID cannot be null")
    @Positive(message = "Product detail ID must be positive")
    private Long newProductDetailId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be a positive number")
    private Integer quantity;
}

