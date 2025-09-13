package com.spring.fit.backend.user.domain.dto;

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

    @NotBlank(message = "City is required")
    private String city;

    private String province;

    private String countryCode;

    private String postalCode;

    private boolean isDefault;
}
