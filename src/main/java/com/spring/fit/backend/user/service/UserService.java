package com.spring.fit.backend.user.service;

import com.spring.fit.backend.product.domain.dto.ProductCardView;
import com.spring.fit.backend.user.domain.dto.UpdateUserRequest;
import com.spring.fit.backend.user.domain.dto.UpdateUserStatusRequest;
import com.spring.fit.backend.user.domain.dto.UserResponse;

import java.util.List;

public interface UserService {
    
    UserResponse updateUser(String email, UpdateUserRequest request);

    UserResponse getCurrentUser(String email);
    
    List<UserResponse> getAllUsers();
    
    UserResponse updateUserStatus(UpdateUserStatusRequest request);
    
    // Recently viewed products methods
    void addProductToRecentlyViewed(String email, long productId);
    
    List<ProductCardView> getRecentlyViewedProducts(String email);
    
    void removeSelectedProductsFromRecentlyViewed(String email, List<Long> productIds);
    
    void clearRecentlyViewedProducts(String email);
}
