package com.spring.fit.backend.user.domain.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.domain.entity.UserRoleEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String email;
    private String username;
    private String phone;
    private LocalDate dob;
    private String avatarUrl;
    private boolean isActive;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private boolean emailVerified;
    private boolean phoneVerified;
    private Set<String> roles;
    private Short rankId;
    private String rankName;

    
    public static UserResponse fromEntity(UserEntity user) {
        Set<String> roleNames = user.getUserRoles().stream()
                .map(UserRoleEntity::getRole)
                .map(role -> role.getRoleName())
                .collect(Collectors.toSet());
        
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .dob(user.getDob())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.isActive())
                .reason(user.getReason())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .roles(roleNames)
                .rankId(user.getRankId())
                .build();
    }
}
