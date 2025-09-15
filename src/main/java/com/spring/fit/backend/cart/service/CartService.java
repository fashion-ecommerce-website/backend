package com.spring.fit.backend.cart.service;

import com.spring.fit.backend.cart.domain.dto.AddToCartRequest;
import com.spring.fit.backend.cart.domain.dto.CartDetailResponse;
import com.spring.fit.backend.cart.domain.dto.UpdateCartItemRequest;

import java.util.List;

public interface CartService {
    
    CartDetailResponse addToCart(String userEmail, AddToCartRequest request);

    List<CartDetailResponse> getCartItems(String userEmail);

    CartDetailResponse updateCartItem(String userEmail, UpdateCartItemRequest request);

    void removeFromCart(String userEmail, Long cartDetailId);

    void removeMultipleFromCart(String userEmail, List<Long> cartDetailIds);

    void clearCart(String userEmail);
    
    long getCartItemCount(String userEmail);
}
