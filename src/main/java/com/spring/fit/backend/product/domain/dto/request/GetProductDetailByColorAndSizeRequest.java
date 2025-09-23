package com.spring.fit.backend.product.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetProductDetailByColorAndSizeRequest {

    @NotNull(message = "Product ID cannot be null")
    @Positive(message = "Product ID must be positive")
    private Long productId;

    @NotNull(message = "Color ID cannot be null")
    @Positive(message = "Color ID must be positive")
    private Short colorId;

    @NotNull(message = "Size ID cannot be null")
    @Positive(message = "Size ID must be positive")
    private Short sizeId;
}
