package com.spring.fit.backend.user.service;

import com.spring.fit.backend.user.domain.dto.UpdateUserRequest;
import com.spring.fit.backend.user.domain.dto.UserResponse;

public interface UserService {
    
    UserResponse updateUser(String email, UpdateUserRequest request);
}
