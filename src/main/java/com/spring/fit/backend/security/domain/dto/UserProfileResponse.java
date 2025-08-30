package com.spring.fit.backend.security.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String email;
    private String username;
    private String phone;
    private String avatarUrl;
    private boolean isActive;
    private boolean emailVerified;
    private boolean phoneVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private List<String> roles;
    private List<AddressDto> addresses;
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressDto {
        private Long id;
        private String fullName;
        private String phone;
        private String line;
        private String ward;
        private String city;
        private String province;
        private String countryCode;
        private String postalCode;
        private boolean isDefault;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
} 