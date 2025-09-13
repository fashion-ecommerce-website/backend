package com.spring.fit.backend.user.controller;

import com.spring.fit.backend.product.domain.dto.ProductCardView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.spring.fit.backend.user.domain.dto.RemoveRecentProductsRequest;
import com.spring.fit.backend.user.domain.dto.UpdateUserRequest;
import com.spring.fit.backend.user.domain.dto.UpdateUserStatusRequest;
import com.spring.fit.backend.user.domain.dto.UserResponse;
import com.spring.fit.backend.user.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping()
    public ResponseEntity<UserResponse> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse currentUser = userService.getCurrentUser(email);
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse updatedUser = userService.updateUser(email, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/status")
    public ResponseEntity<UserResponse> updateUserStatus(@Valid @RequestBody UpdateUserStatusRequest request) {
        UserResponse updatedUser = userService.updateUserStatus(request);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/recently/{productId}")
    public ResponseEntity<Void> addProductToRecently(
            @PathVariable @Positive(message = "Product ID phải là số dương") long productId) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.addProductToRecentlyViewed(email, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recently")
    public ResponseEntity<List<ProductCardView>> getRecentlyProducts() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ProductCardView> recentProducts = userService.getRecentlyViewedProducts(email);
        return ResponseEntity.ok(recentProducts);
    }

    @DeleteMapping("/recently/remove")
    public ResponseEntity<Void> removeSelectedRecentProducts(
            @RequestBody @Valid RemoveRecentProductsRequest request) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.removeSelectedProductsFromRecentlyViewed(email, request.getProductIds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/recently/clear")
    public ResponseEntity<Void> clearRecentlyViewed() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.clearRecentlyViewedProducts(email);
        return ResponseEntity.ok().build();
    }
}
