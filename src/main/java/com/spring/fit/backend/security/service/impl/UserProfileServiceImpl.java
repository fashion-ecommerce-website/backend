package com.spring.fit.backend.security.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.security.domain.dto.UpdateUserRequest;
import com.spring.fit.backend.security.domain.dto.UserProfileResponse;
import com.spring.fit.backend.security.domain.entity.Address;
import com.spring.fit.backend.security.domain.entity.User;
import com.spring.fit.backend.security.repository.AddressRepository;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.security.service.UserProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserProfileServiceImpl implements UserProfileService {
    
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    public boolean isProfileComplete(String userEmail) {
        log.info("Checking profile completeness for user: {}", userEmail);
        
        User user = userRepository.findActiveUserByEmail(userEmail)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Kiểm tra các trường bắt buộc từ database schema
        boolean hasBasicInfo = user.getUsername() != null && !user.getUsername().trim().isEmpty() &&
                              user.getPhone() != null && !user.getPhone().trim().isEmpty();
        
        boolean hasAddress = addressRepository.countByUserId(user.getId()) > 0;
        
        boolean isVerified = user.isEmailVerified() && user.isPhoneVerified();
        
        return hasBasicInfo && hasAddress && isVerified;
    }
    
    @Override
    public int getProfileCompletionPercentage(String userEmail) {
        log.info("Calculating profile completion percentage for user: {}", userEmail);
        
        User user = userRepository.findActiveUserByEmail(userEmail)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
        
        int totalPoints = 6; // Total possible points based on actual DB fields
        int earnedPoints = 0;
        
        // Username (1 point)
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            earnedPoints++;
        }
        
        // Phone (1 point)
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            earnedPoints++;
        }
        
        // Avatar (1 point)
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()) {
            earnedPoints++;
        }
        
        // Has address (1 point)
        if (addressRepository.countByUserId(user.getId()) > 0) {
            earnedPoints++;
        }
        
        // Email verified (1 point)
        if (user.isEmailVerified()) {
            earnedPoints++;
        }
        
        // Phone verified (1 point)
        if (user.isPhoneVerified()) {
            earnedPoints++;
        }
        
        return (earnedPoints * 100) / totalPoints;
    }
    
    @Override
    public UserProfileResponse getUserProfile(String userEmail) {
        log.info("Getting user profile for email: {}", userEmail);
        
        // Sử dụng method với JOIN FETCH để tránh LazyInitializationException
        User user = userRepository.findActiveUserByEmailWithRoles(userEmail)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
        
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
        
        return buildUserProfileResponse(user);
    }
    
    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(String userEmail, UpdateUserRequest request) {
        log.info("Updating user profile for email: {}", userEmail);
        
        User user = userRepository.findActiveUserByEmailWithRoles(userEmail)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Validate unique username nếu có thay đổi
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ErrorException(HttpStatus.BAD_REQUEST, "Username already exists");
            }
            user.setUsername(request.getUsername());
        }
        
        // Cập nhật phone nếu có
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
            // Reset phone verification khi thay đổi số điện thoại
            user.setPhoneVerified(false);
        }
        
        // Cập nhật avatar URL nếu có
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        user = userRepository.save(user);
        log.info("Updated user profile for email: {}", userEmail);
        
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
        
        return buildUserProfileResponse(user);
    }

    /**
     * Xây dựng UserProfileResponse từ User entity
     */
    private UserProfileResponse buildUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.isActive())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .roles(user.getUserRoles().stream()
                        .map(userRole -> userRole.getRole().getRoleName())
                        .collect(Collectors.toList()))
                .build();
    }



} 