package com.spring.fit.backend.voucher.domain.dto;

import com.spring.fit.backend.common.enums.VoucherType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherByUserResponse {

    private Long id;
    private String name;
    private String code;
    private VoucherType type;
    private BigDecimal value;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderAmount;
    private Integer usageLimitTotal;
    private Integer usageLimitPerUser;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private boolean isAvailable;
    private String message;
}



