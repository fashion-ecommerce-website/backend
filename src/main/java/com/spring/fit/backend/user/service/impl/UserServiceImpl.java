package com.spring.fit.backend.user.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.function.Function;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.product.domain.dto.response.ProductCardView;
import com.spring.fit.backend.product.domain.dto.response.ProductCardWithPromotionResponse;
import com.spring.fit.backend.product.service.ProductService;
import com.spring.fit.backend.user.service.RecentViewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.user.domain.dto.request.UpdateUserRequest;
import com.spring.fit.backend.user.domain.dto.request.UpdateUserStatusRequest;
import com.spring.fit.backend.user.domain.dto.response.UserResponse;
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
        log.info("Inside UserServiceImpl.getCurrentUser email={}", email);
        if (isEmpty(email)) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Invalid email");
        }
        UserEntity user = userRepository.findActiveUserByEmail(email.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
        return UserResponse.fromEntity(user);
    }

    @Override
    public UserResponse updateUser(String email, UpdateUserRequest request) {
        log.info("Inside UserServiceImpl.updateUser email={}", email);
        
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
        log.info("Inside UserServiceImpl.updateUser success email={}", updatedUser.getEmail());
        
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    public PageResult<UserResponse> getAllUsers(int page, int pageSize) {
        log.info("Inside UserServiceImpl.getAllUsers page={}, pageSize={}", page, pageSize);
        
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserEntity> userPage = userRepository.findAll(pageable);
        
        Page<UserResponse> responsePage = userPage.map(UserResponse::fromEntity);
        
        PageResult<UserResponse> result = PageResult.from(responsePage);
        log.info("Inside UserServiceImpl.getAllUsers success totalItems={}, totalPages={}", 
                result.totalItems(), result.totalPages());
        return result;
    }

    @Override
    public UserResponse updateUserStatus(UpdateUserStatusRequest request) {
        log.info("Inside UserServiceImpl.updateUserStatus userId={}, isActive={}", request.getUserId(), request.getIsActive());
        
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
        log.info("Inside UserServiceImpl.updateUserStatus success userId={}, isActive={}", updatedUser.getId(), updatedUser.isActive());
        
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
        log.info("Inside UserServiceImpl.addProductToRecentlyViewed email={}, productId={}", email, productId);
        
        try {
            UserResponse user = getCurrentUser(email);
            recentViewService.addViewed(user.getId(), productId);
            
            log.info("Inside UserServiceImpl.addProductToRecentlyViewed success email={}, productId={}", email, productId);
            
        } catch (Exception e) {
            log.error("Inside UserServiceImpl.addProductToRecentlyViewed error email={}, productId={}, message={}", email, productId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<ProductCardView> getRecentlyViewedProducts(String email) {
        log.info("Inside UserServiceImpl.getRecentlyViewedProducts email={}", email);
        
        try {
            UserResponse user = getCurrentUser(email);
            
            List<Long> recentProductIds = recentViewService.getRecentIds(user.getId());
            
            if (recentProductIds.isEmpty()) {
                log.info("Inside UserServiceImpl.getRecentlyViewedProducts none email={}", email);
                return List.of();
            }
            
            List<ProductCardView> recentProducts = productService.getRecentlyViewedProducts(recentProductIds, user.getId());

            // Danh sách đã sắp xếp theo thứ tự "viewed"
            Map<Long, ProductCardView> byId =
                recentProducts.stream()
                            .collect(Collectors.toMap(ProductCardView::getDetailId, Function.identity()));

            List<ProductCardView> ordered =
                recentProductIds.stream()
                                .map(byId::get)
                                .filter(Objects::nonNull)
                                .toList();

            log.info("Inside UserServiceImpl.getRecentlyViewedProducts success email={}, count={}", email, ordered.size());
            return ordered;
            
        } catch (Exception e) {
            log.error("Inside UserServiceImpl.getRecentlyViewedProducts error email={}, message={}", email, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<ProductCardWithPromotionResponse> getRecentlyViewedProductsWithPromotion(String email) {
        log.info("Inside UserServiceImpl.getRecentlyViewedProductsWithPromotion email={}", email);
        
        try {
            UserResponse user = getCurrentUser(email);
            
            List<Long> recentProductIds = recentViewService.getRecentIds(user.getId());
            
            if (recentProductIds.isEmpty()) {
                log.info("Inside UserServiceImpl.getRecentlyViewedProductsWithPromotion none email={}", email);
                return List.of();
            }
            
            List<ProductCardWithPromotionResponse> recentProducts = productService.getRecentlyViewedProductsWithPromotion(recentProductIds, user.getId());

            // Danh sách đã sắp xếp theo thứ tự "viewed"
            Map<Long, ProductCardWithPromotionResponse> byId =
                recentProducts.stream()
                            .collect(Collectors.toMap(ProductCardWithPromotionResponse::getDetailId, Function.identity()));

            List<ProductCardWithPromotionResponse> ordered =
                recentProductIds.stream()
                                .map(byId::get)
                                .filter(Objects::nonNull)
                                .toList();

            log.info("Inside UserServiceImpl.getRecentlyViewedProductsWithPromotion success email={}, count={}", email, ordered.size());
            return ordered;
            
        } catch (Exception e) {
            log.error("Inside UserServiceImpl.getRecentlyViewedProductsWithPromotion error email={}, message={}", email, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void removeSelectedProductsFromRecentlyViewed(String email, List<Long> productIds) {
        log.info("Inside UserServiceImpl.removeSelectedProductsFromRecentlyViewed email={}, productIds={}", email, productIds);
        
        try {
            UserResponse user = getCurrentUser(email);
            recentViewService.removeSelected(user.getId(), productIds);
            
            log.info("Inside UserServiceImpl.removeSelectedProductsFromRecentlyViewed success email={}, removedCount={}", email, productIds.size());
            
        } catch (Exception e) {
            log.error("Inside UserServiceImpl.removeSelectedProductsFromRecentlyViewed error email={}, message={}", email, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void clearRecentlyViewedProducts(String email) {
        log.info("Inside UserServiceImpl.clearRecentlyViewedProducts email={}", email);
        
        try {
            UserResponse user = getCurrentUser(email);
            recentViewService.clearAll(user.getId());
            
            log.info("Inside UserServiceImpl.clearRecentlyViewedProducts success email={}", email);
            
        } catch (Exception e) {
            log.error("Inside UserServiceImpl.clearRecentlyViewedProducts error email={}, message={}", email, e.getMessage(), e);
            throw e;
        }}
}
