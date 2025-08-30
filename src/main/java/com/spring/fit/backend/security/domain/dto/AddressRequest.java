package com.spring.fit.backend.security.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Address line is required")
    @Size(max = 500, message = "Address line must not exceed 500 characters")
    private String line;
    
    @Size(max = 100, message = "Ward must not exceed 100 characters")
    private String ward;
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @Size(max = 100, message = "Province must not exceed 100 characters")
    private String province;
    
    @Size(max = 10, message = "Country code must not exceed 10 characters")
    private String countryCode;
    
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;
    
    private boolean isDefault;
} 