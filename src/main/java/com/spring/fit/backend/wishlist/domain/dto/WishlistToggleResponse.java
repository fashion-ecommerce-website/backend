package com.spring.fit.backend.wishlist.domain.dto;

import com.spring.fit.backend.product.domain.dto.ProductDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishlistToggleResponse {
    private Long wishlistId;
    private boolean status; // true nếu vừa add, false nếu vừa xóa
    public ProductDetailResponse productDetail;
}
