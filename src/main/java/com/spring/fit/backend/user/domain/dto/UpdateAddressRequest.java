package com.spring.fit.backend.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAddressRequest {

    private String fullName;

    private String phone;

    private String line;

    private String ward;

    private String city;

    private String province;

    private String countryCode;

    private String postalCode;

    private Boolean isDefault;
}
