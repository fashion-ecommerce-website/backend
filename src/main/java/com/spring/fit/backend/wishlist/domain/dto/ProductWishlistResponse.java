package com.spring.fit.backend.wishlist.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductWishlistResponse {
    private Long detailId;
    private Long id;                // id của product
    private String title;          // từ Product
    private String color;          // từ ProductDetail.color
    private String size;           // từ ProductDetail.size
    private BigDecimal price;      // từ ProductDetail
}
