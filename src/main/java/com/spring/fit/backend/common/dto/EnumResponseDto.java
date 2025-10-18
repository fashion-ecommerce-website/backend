package com.spring.fit.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnumResponseDto {
    private Map<String, String> orderStatus;
    private Map<String, String> paymentMethod;
    private Map<String, String> paymentStatus;
    private Map<String, String> fulfillmentStatus;
    private Map<String, String> voucherUsageStatus;
    private Map<String, String> audienceType;
    private Map<String, String> voucherType;
}

