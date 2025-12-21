package com.spring.fit.backend.user.controller;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.product.domain.dto.response.ProductCardWithPromotionResponse;
import com.spring.fit.backend.user.domain.dto.response.UserRankResponse;
import com.spring.fit.backend.user.service.UserRankService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.spring.fit.backend.user.domain.dto.request.RemoveRecentProductsRequest;
import com.spring.fit.backend.user.domain.dto.request.UpdateUserRequest;
import com.spring.fit.backend.user.domain.dto.request.UpdateUserStatusRequest;
import com.spring.fit.backend.user.domain.dto.response.UserResponse;
import com.spring.fit.backend.user.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRankService userRankService;

    @GetMapping()
    public ResponseEntity<UserResponse> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse currentUser = userService.getCurrentUser(email);
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/all")
    public ResponseEntity<PageResult<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0")
            @Max(value = 100, message = "Page cannot exceed 100")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "PageSize must be >= 1")
            @Max(value = 100, message = "PageSize cannot exceed 100")
            int pageSize,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Boolean active) {
        PageResult<UserResponse> users = userService.getAllUsers(page, pageSize, keyword, active);
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
    public ResponseEntity<List<ProductCardWithPromotionResponse>> getRecentlyProducts() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ProductCardWithPromotionResponse> recentProducts = userService.getRecentlyViewedProductsWithPromotion(email);
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

    @GetMapping("/ranks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserRankResponse>> getAllUserRanks() {
        List<UserRankResponse> userRanks = userRankService.getAllUserRanks();
        return ResponseEntity.ok(userRanks);
    }
}
