package com.spring.fit.backend.user.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    // GHN ward code (mã phường/xã theo GHN API)
    private String wardCode;

    private String city;

    private String province;

    // GHN district ID (mã quận/huyện theo GHN API)
    private Integer districtId;

    private String countryCode;

    private String postalCode;

    @JsonProperty("isDefault")
    private Boolean isDefault;
}
