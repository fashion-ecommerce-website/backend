package com.spring.fit.backend.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.fit.backend.security.domain.dto.UpdateUserRequest;
import com.spring.fit.backend.security.domain.dto.UserProfileResponse;
import com.spring.fit.backend.security.service.UserProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {
    
    private final UserProfileService userProfileService;


    /**
     * Lấy thông tin profile cơ bản (original endpoint)
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
        log.info("Getting user profile for: {}", authentication.getName());
        
        UserProfileResponse profile = userProfileService.getUserProfile(authentication.getName());
        
        return ResponseEntity.ok(profile);
    }

    /**
     * Cập nhật profile cơ bản (original endpoint)
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user profile for: {}", authentication.getName());

        UserProfileResponse profile = userProfileService.updateUserProfile(authentication.getName(), request);
        return ResponseEntity.ok(profile);
    }

    
    /**
     * Kiểm tra profile completion status
     */
    @GetMapping("/profile/completion")
    public ResponseEntity<ProfileCompletionResponse> getProfileCompletion(Authentication authentication) {
        log.info("Getting profile completion for: {}", authentication.getName());
        
        boolean isComplete = userProfileService.isProfileComplete(authentication.getName());
        int percentage = userProfileService.getProfileCompletionPercentage(authentication.getName());
        
        ProfileCompletionResponse response = ProfileCompletionResponse.builder()
                .isComplete(isComplete)
                .completionPercentage(percentage)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Response DTO cho profile completion
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ProfileCompletionResponse {
        private boolean isComplete;
        private int completionPercentage;
    }
} 