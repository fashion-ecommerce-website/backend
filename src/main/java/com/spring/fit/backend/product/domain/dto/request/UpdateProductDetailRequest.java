package com.spring.fit.backend.product.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductDetailRequest {

    @NotNull(message = "Color ID cannot be null")
    @Positive(message = "Color ID must be positive")
    private Short colorId;

    @NotNull(message = "Size ID cannot be null")
    @Positive(message = "Size ID must be positive")
    private Short sizeId;

    @NotNull(message = "Product price cannot be null")
    @Positive(message = "Product price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}
