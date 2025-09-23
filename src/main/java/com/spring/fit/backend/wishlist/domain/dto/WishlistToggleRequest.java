package com.spring.fit.backend.wishlist.domain.dto;

import lombok.Data;

@Data
public class WishlistToggleRequest {
    private Long productId;
    private short colorId;
    private short sizeId;
}

