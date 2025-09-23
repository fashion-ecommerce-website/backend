package com.spring.fit.backend.wishlist.controller;

import com.spring.fit.backend.product.domain.dto.ProductDetailResponse;
import com.spring.fit.backend.wishlist.domain.dto.ProductWishlistResponse;
import com.spring.fit.backend.wishlist.domain.dto.WishlistToggleRequest;
import com.spring.fit.backend.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * Lấy danh sách wishlist của user
     */
    @GetMapping
    public ResponseEntity<List<ProductWishlistResponse>> getWishlist() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ProductWishlistResponse> wishlist = wishlistService.getUserWishlist(email);
        return ResponseEntity.ok(wishlist);
    }

    /**
     * Toggle wishlist (thêm hoặc xoá nếu đã tồn tại)
     */
    @PostMapping("/toggle")
    public ResponseEntity<ProductWishlistResponse> toggleWishlist(
            @RequestBody WishlistToggleRequest request
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ProductWishlistResponse response = wishlistService.toggleWishlist(
                email,
                request.getProductId(),
                request.getColorId(),
                request.getSizeId()
        );
        return ResponseEntity.ok(response);
    }

}
