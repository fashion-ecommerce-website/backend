package com.spring.fit.backend.voucher.domain.dto;

import com.spring.fit.backend.common.enums.VoucherType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherValidateResponse {
    
    private boolean valid;
    private Long voucherId;
    private String code;
    private VoucherType type;
    private BigDecimal value;
    private BigDecimal maxDiscount;
    private BigDecimal discountPreview;
    private String message; // Error message if voucher is invalid
}
