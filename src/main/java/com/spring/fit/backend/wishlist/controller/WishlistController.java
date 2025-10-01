package com.spring.fit.backend.wishlist.controller;

import com.spring.fit.backend.product.domain.dto.response.ProductDetailResponse;

import com.spring.fit.backend.wishlist.domain.dto.WishlistToggleResponse;
import com.spring.fit.backend.wishlist.service.WishlistService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @PutMapping("/toggle/{detailId}")
    public ResponseEntity<WishlistToggleResponse> toggleWishlist(
            @PathVariable("detailId") @Positive(message = "Detail ID must be positive") Long productDetailId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        WishlistToggleResponse response = wishlistService.toggleWishlist(email, productDetailId);
        return ResponseEntity.ok(response);
    }


    @GetMapping()
    public ResponseEntity<List<ProductDetailResponse>> getWishlistByUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ProductDetailResponse> wishlist = wishlistService.getWishlistByUserEmail(email);
        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearWishlists() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        wishlistService.clearWishlistByUser(email);
        return ResponseEntity.noContent().build();
    }

}
