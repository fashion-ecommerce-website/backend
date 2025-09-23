package com.spring.fit.backend.wishlist.service;

import com.spring.fit.backend.product.domain.dto.ProductDetailResponse;
import com.spring.fit.backend.wishlist.domain.dto.ProductWishlistResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface WishlistService {
     List<ProductWishlistResponse> getUserWishlist(String email);
     ProductWishlistResponse toggleWishlist(String userEmail, Long productId, short colorId, short sizeId);
}
