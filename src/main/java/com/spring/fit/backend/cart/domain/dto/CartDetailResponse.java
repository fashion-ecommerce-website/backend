package com.spring.fit.backend.cart.domain.dto;

import com.spring.fit.backend.cart.domain.entity.CartDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDetailResponse {

    private Long id;
    private Long productDetailId;
    private String productTitle;
    private String productSlug;
    private String colorName;
    private String sizeName;
    private BigDecimal price;
    private Integer quantity;
    private Integer availableQuantity;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartDetailResponse fromEntity(CartDetail cartDetail) {
        String imageUrl = null;
        if (cartDetail.getProductDetail().getProductImages() != null 
            && !cartDetail.getProductDetail().getProductImages().isEmpty()) {
            imageUrl = cartDetail.getProductDetail().getProductImages().iterator().next().getImage().getUrl();
        }

        return CartDetailResponse.builder()
                .id(cartDetail.getId())
                .productDetailId(cartDetail.getProductDetail().getId())
                .productTitle(cartDetail.getProductDetail().getProduct().getTitle())
                .productSlug(cartDetail.getProductDetail().getSlug())
                .colorName(cartDetail.getProductDetail().getColor().getName())
                .sizeName(cartDetail.getProductDetail().getSize().getCode())
                .price(cartDetail.getProductDetail().getPrice())
                .quantity(cartDetail.getQuantity())
                .availableQuantity(cartDetail.getProductDetail().getQuantity())
                .imageUrl(imageUrl)
                .createdAt(cartDetail.getCreatedAt())
                .updatedAt(cartDetail.getUpdatedAt())
                .build();
    }
}

