package com.spring.fit.backend.cart.domain.dto;

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
public class UpdateCartItemRequest {

    @NotNull(message = "Cart detail ID không được để trống")
    @Positive(message = "Cart detail ID phải là số dương")
    private Long cartDetailId;

    @NotNull(message = "Product detail ID mới không được để trống")
    @Positive(message = "Product detail ID phải là số dương")
    private Long newProductDetailId;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải là số dương")
    private Integer quantity;
}

