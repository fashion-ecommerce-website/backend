package com.spring.fit.backend.user.domain.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spring.fit.backend.user.domain.entity.AddressEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    
    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String line;
    private String ward;
    private String wardCode; // GHN ward code
    private String city;
    private Integer districtId; // GHN district ID
    private String countryCode;
    private String postalCode;
    
    @JsonProperty("isDefault")
    private boolean isDefault;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static AddressResponse fromEntity(AddressEntity address) {
        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUser().getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .line(address.getLine())
                .ward(address.getWard())
                .wardCode(address.getWardCode())
                .city(address.getCity())
                .districtId(address.getDistrictId())
                .countryCode(address.getCountryCode())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
