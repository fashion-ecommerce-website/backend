package com.spring.fit.backend.cart.domain.dto;

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

    @NotNull(message = "Danh sách cart detail ID không được null")
    @NotEmpty(message = "Danh sách cart detail ID không được rỗng")
    private List<@Positive(message = "Cart detail ID phải là số dương") Long> cartDetailIds;
}

