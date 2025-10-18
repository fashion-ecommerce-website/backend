package com.spring.fit.backend.common.service;

import com.spring.fit.backend.common.dto.EnumResponseDto;
import com.spring.fit.backend.common.enums.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnumService {

    public EnumResponseDto getAllEnums() {
        return new EnumResponseDto(
            getEnumValues(OrderStatus.class),
            getEnumValues(PaymentMethod.class),
            getEnumValues(PaymentStatus.class),
            getEnumValues(FulfillmentStatus.class),
            getEnumValues(VoucherUsageStatus.class),
            getEnumValues(AudienceType.class),
            getEnumValues(VoucherType.class)
        );
    }

    private <T extends Enum<T>> Map<String, String> getEnumValues(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(
                    Enum::name,
                    enumValue -> {
                        try {
                            // Sử dụng reflection để gọi method getValue() nếu có
                            return (String) enumClass.getMethod("getValue").invoke(enumValue);
                        } catch (Exception e) {
                            // Nếu không có method getValue(), sử dụng name()
                            return enumValue.name();
                        }
                    }
                ));
    }
}

