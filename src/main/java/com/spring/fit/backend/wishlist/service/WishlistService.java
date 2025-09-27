package com.spring.fit.backend.wishlist.service;

import com.spring.fit.backend.product.domain.dto.ProductDetailResponse;
import com.spring.fit.backend.wishlist.domain.dto.ProductWishlistResponse;
import com.spring.fit.backend.wishlist.domain.dto.WishlistToggleResponse;

import java.util.List;

public interface WishlistService {
      List<ProductDetailResponse> getWishlistByUserId(String userEmail);
      WishlistToggleResponse toggleWishlist(String userEmail, Long detailId);
}