package com.spring.fit.backend.user.service.impl;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.user.domain.dto.UpdateUserRequest;
import com.spring.fit.backend.user.domain.dto.UserResponse;
import com.spring.fit.backend.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;

    @Override
    public UserResponse updateUser(String email, UpdateUserRequest request) {
        log.info("Updating user: {}", email);
        
        // Validate
        if (request == null || isEmpty(email)) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Invalid request or email");
        }
        
        // Find user
        UserEntity user = userRepository.findActiveUserByEmail(email.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Update fields
        updateField(user, request.getEmail(), user.getEmail(), 
                newEmail -> user.setEmail(newEmail), 
                newEmail -> userRepository.existsByEmail(newEmail));
        
        updateField(user, request.getUsername(), user.getUsername(), 
                newUsername -> user.setUsername(newUsername), 
                newUsername -> userRepository.existsByUsername(newUsername));
        
        updateField(user, request.getPhone(), user.getPhone(), 
                newPhone -> user.setPhone(newPhone), null);
        
        updateField(user, request.getAvatarUrl(), user.getAvatarUrl(), 
                newAvatarUrl -> user.setAvatarUrl(newAvatarUrl), null);
        
        updateField(user, request.getReason(), user.getReason(), 
                newReason -> user.setReason(newReason), null);
        
        // Save and return
        UserEntity updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getEmail());
        
        return UserResponse.fromEntity(updatedUser);
    }

    private void updateField(UserEntity user, String newValue, String currentValue, 
                           Consumer<String> setter, 
                           Function<String, Boolean> duplicateChecker) {
        if (!isEmpty(newValue) && !newValue.trim().equals(currentValue)) {
            String value = newValue.trim();
            
            // Check duplicate if needed
            if (duplicateChecker != null && duplicateChecker.apply(value)) {
                throw new ErrorException(HttpStatus.CONFLICT, "Value already exists: " + value);
            }
            
            setter.accept(value);
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
