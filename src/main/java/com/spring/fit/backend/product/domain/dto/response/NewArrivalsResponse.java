package com.spring.fit.backend.product.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewArrivalsResponse {
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private List<ProductCardWithPromotionResponse> products;
}

