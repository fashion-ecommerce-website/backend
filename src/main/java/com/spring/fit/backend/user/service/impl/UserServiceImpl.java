package com.spring.fit.backend.user.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.function.Function;

import com.spring.fit.backend.product.domain.dto.ProductCardView;
import com.spring.fit.backend.product.service.ProductService;
import com.spring.fit.backend.user.service.RecentViewService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.user.domain.dto.UpdateUserRequest;
import com.spring.fit.backend.user.domain.dto.UpdateUserStatusRequest;
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
    private final RecentViewService recentViewService;
    private final ProductService productService;

    @Override
    public UserResponse getCurrentUser(String email) {
        log.info("Fetching current user: {}", email);
        if (isEmpty(email)) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Invalid email");
        }
        UserEntity user = userRepository.findActiveUserByEmail(email.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
        return UserResponse.fromEntity(user);
    }

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
        
        updateField(user, request.getUsername(), user.getUsername(), 
                newUsername -> user.setUsername(newUsername), 
                newUsername -> userRepository.existsByUsername(newUsername));

        String newDobStr = request.getDob() != null ? request.getDob().toString() : null;
        String currentDobStr = user.getDob() != null ? user.getDob().toString() : null;

        updateField(user, newDobStr, currentDobStr,
                str -> user.setDob(LocalDate.parse(str)),
                null);

        updateField(user, request.getPhone(), user.getPhone(), 
                newPhone -> user.setPhone(newPhone), null);
        
        updateField(user, request.getAvatarUrl(), user.getAvatarUrl(),
                newAvatarUrl -> user.setAvatarUrl(newAvatarUrl), null);

        // Save and return
        UserEntity updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getEmail());
        
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        List<UserEntity> users = userRepository.findAll();
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::fromEntity)
                .toList();
        log.info("Found {} users", userResponses.size());
        return userResponses;
    }

    @Override
    public UserResponse updateUserStatus(UpdateUserStatusRequest request) {
        log.info("Updating user status for user ID: {} to status: {}", request.getUserId(), request.getIsActive());
        
        // Validate request
        if (request == null || request.getUserId() == null || request.getIsActive() == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Invalid request: userId and isActive are required");
        }
        
        // Find user by ID
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found with ID: " + request.getUserId()));
        
        // Update status
        user.setActive(request.getIsActive());
        
        // Save and return
        UserEntity updatedUser = userRepository.save(user);
        log.info("User status updated for user ID: {} to status: {}", updatedUser.getId(), updatedUser.isActive());
        
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
    
    // Recently viewed products methods
    
    @Override
    public void addProductToRecentlyViewed(String email, long productId) {
        log.info("Adding product {} to recently viewed for user: {}", productId, email);
        
        try {
            UserResponse user = getCurrentUser(email);
            recentViewService.addViewed(user.getId(), productId);
            
            log.info("Successfully added product {} to recently viewed for user: {}", productId, email);
            
        } catch (Exception e) {
            log.error("Error adding product {} to recently viewed for user {}: {}", productId, email, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<ProductCardView> getRecentlyViewedProducts(String email) {
        log.info("Getting recently viewed products for user: {}", email);
        
        try {
            UserResponse user = getCurrentUser(email);
            
            List<Long> recentProductIds = recentViewService.getRecentIds(user.getId());
            
            if (recentProductIds.isEmpty()) {
                log.info("No recently viewed products found for user: {}", email);
                return List.of();
            }
            
            List<ProductCardView> recentProducts = productService.getRecentlyViewedProducts(recentProductIds);

            // Danh sách đã sắp xếp theo thứ tự "viewed"
            Map<Long, ProductCardView> byId =
                recentProducts.stream()
                            .collect(Collectors.toMap(ProductCardView::getDetailId, Function.identity()));

            List<ProductCardView> ordered =
                recentProductIds.stream()
                                .map(byId::get)
                                .filter(Objects::nonNull)
                                .toList();

            log.info("Found {} recently viewed products for user: {}", ordered.size(), email);
            return ordered;
            
        } catch (Exception e) {
            log.error("Error getting recently viewed products for user {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void removeSelectedProductsFromRecentlyViewed(String email, List<Long> productIds) {
        log.info("Removing selected products {} from recently viewed for user: {}", productIds, email);
        
        try {
            UserResponse user = getCurrentUser(email);
            recentViewService.removeSelected(user.getId(), productIds);
            
            log.info("Successfully removed {} products from recently viewed for user: {}", productIds.size(), email);
            
        } catch (Exception e) {
            log.error("Error removing selected products from recently viewed for user {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void clearRecentlyViewedProducts(String email) {
        log.info("Clearing all recently viewed products for user: {}", email);
        
        try {
            UserResponse user = getCurrentUser(email);
            recentViewService.clearAll(user.getId());
            
            log.info("Successfully cleared all recently viewed products for user: {}", email);
            
        } catch (Exception e) {
            log.error("Error clearing recently viewed products for user {}: {}", email, e.getMessage(), e);
            throw e;
        }}
}
