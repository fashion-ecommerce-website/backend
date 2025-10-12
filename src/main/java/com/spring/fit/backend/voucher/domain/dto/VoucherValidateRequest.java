package com.spring.fit.backend.voucher.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherValidateRequest {
    
    @NotBlank(message = "Voucher code is required")
    private String code;
    
    @NotNull(message = "Subtotal is required")
    @Positive(message = "Subtotal must be positive")
    private Double subtotal;
}


