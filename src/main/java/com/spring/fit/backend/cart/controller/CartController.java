package com.spring.fit.backend.cart.controller;

import com.spring.fit.backend.cart.domain.dto.AddToCartRequest;
import com.spring.fit.backend.cart.domain.dto.CartDetailResponse;
import com.spring.fit.backend.cart.domain.dto.RemoveFromCartRequest;
import com.spring.fit.backend.cart.domain.dto.UpdateCartItemRequest;
import com.spring.fit.backend.cart.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartDetailResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        String email = getCurrentUserEmail();
        CartDetailResponse response = cartService.addToCart(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CartDetailResponse>> getCartItems() {
        String email = getCurrentUserEmail();
        List<CartDetailResponse> cartItems = cartService.getCartItems(email);
        return ResponseEntity.ok(cartItems);
    }

    @PutMapping("/update")
    public ResponseEntity<CartDetailResponse> updateCartItemWithProductChange(@Valid @RequestBody UpdateCartItemRequest request) {
        String email = getCurrentUserEmail();
        CartDetailResponse response = cartService.updateCartItem(email, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cartDetailId}")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable @Positive(message = "Cart detail ID must be positive") Long cartDetailId) {
        String email = getCurrentUserEmail();
        cartService.removeFromCart(email, cartDetailId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/remove-multiple")
    public ResponseEntity<Void> removeMultipleFromCart(@Valid @RequestBody RemoveFromCartRequest request) {
        String email = getCurrentUserEmail();
        cartService.removeMultipleFromCart(email, request.getCartDetailIds());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        String email = getCurrentUserEmail();
        cartService.clearCart(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCartItemCount() {
        String email = getCurrentUserEmail();
        long count = cartService.getCartItemCount(email);
        return ResponseEntity.ok(Map.of("count", count));
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}