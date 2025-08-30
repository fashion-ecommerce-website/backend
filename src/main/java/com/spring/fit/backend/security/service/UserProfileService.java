package com.spring.fit.backend.security.service;

import com.spring.fit.backend.security.domain.dto.UpdateUserRequest;
import com.spring.fit.backend.security.domain.dto.UserProfileResponse;

public interface UserProfileService {
    
    /**
     * Lấy thông tin profile của user - Original method
     */
    UserProfileResponse getUserProfile(String userEmail);
    

    /**
     * Cập nhật thông tin profile của user - Original method  
     */
    UserProfileResponse updateUserProfile(String userEmail, UpdateUserRequest request);

    /**
     * Kiểm tra và validate user profile completeness
     */
    boolean isProfileComplete(String userEmail);
    
    /**
     * Lấy profile completion percentage
     */
    int getProfileCompletionPercentage(String userEmail);


} 