package com.spring.fit.backend.product.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardWithPromotionResponse {
    private Long productId;
    private Long detailId;
    private String productTitle;
    private String productSlug;
    private String colorName;
    private BigDecimal price;          // base price
    private BigDecimal finalPrice;     // after promotion
    private Integer percentOff;        // integer percent
    private Long promotionId;          // nullable
    private String promotionName;      // nullable
    private Integer quantity;
    private List<String> colors;
    private List<String> imageUrls;
}



