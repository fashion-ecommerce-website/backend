package com.spring.fit.backend.voucher.domain.dto;

import com.spring.fit.backend.common.enums.AudienceType;
import com.spring.fit.backend.common.enums.VoucherType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class AdminVoucherResponse {
    Long id;
    String name;
    String code;
    VoucherType type;
    BigDecimal value;
    BigDecimal maxDiscount;
    BigDecimal minOrderAmount;
    Integer usageLimitTotal;
    Integer usageLimitPerUser;
    LocalDateTime startAt;
    LocalDateTime endAt;
    Boolean isActive;
    AudienceType audienceType;
    List<Short> rankIds;
    Long usageCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

