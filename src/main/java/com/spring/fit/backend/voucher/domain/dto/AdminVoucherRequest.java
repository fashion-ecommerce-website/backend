package com.spring.fit.backend.voucher.domain.dto;

import com.spring.fit.backend.common.enums.AudienceType;
import com.spring.fit.backend.common.enums.VoucherType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminVoucherRequest {
    @NotBlank
    private String name;

    @NotNull
    private VoucherType type;

    @NotNull
    @Digits(integer = 12, fraction = 2)
    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal value;

    @Digits(integer = 12, fraction = 2)
    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal maxDiscount;

    @Digits(integer = 12, fraction = 2)
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal minOrderAmount;

    @PositiveOrZero
    private Integer usageLimitTotal;

    @PositiveOrZero
    private Integer usageLimitPerUser;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    @NotNull
    private AudienceType audienceType;

    @NotNull
    private Boolean isActive;

    private List<Short> rankIds;
}

