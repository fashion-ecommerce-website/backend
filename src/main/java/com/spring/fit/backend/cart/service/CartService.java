package com.spring.fit.backend.cart.service;

import com.spring.fit.backend.cart.domain.dto.request.AddToCartRequest;
import com.spring.fit.backend.cart.domain.dto.response.CartDetailResponse;
import com.spring.fit.backend.cart.domain.dto.response.CartDetailWithPromotionResponse;
import com.spring.fit.backend.cart.domain.dto.request.UpdateCartRequest;

import java.util.List;

public interface CartService {
    
    CartDetailResponse addToCart(String userEmail, AddToCartRequest request);

    List<CartDetailResponse> getCartItems(String userEmail);

    List<CartDetailWithPromotionResponse> getCartItemsWithPromotion(String userEmail);

    CartDetailResponse updateCartItem(String userEmail, UpdateCartRequest request);

    void removeFromCart(String userEmail, List<Long> cartDetailIds);

    void clearCart(String userEmail);
    
    long getCartItemCount(String userEmail);
}
