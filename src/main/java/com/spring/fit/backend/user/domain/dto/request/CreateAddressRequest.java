package com.spring.fit.backend.user.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAddressRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Address line is required")
    private String line;

    private String ward;

    // GHN ward code (mã phường/xã theo GHN API)
    private String wardCode;

    @NotBlank(message = "City is required")
    private String city;

    private String province;

    // GHN district ID (mã quận/huyện theo GHN API)
    private Integer districtId;

    private String countryCode;

    private String postalCode;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
