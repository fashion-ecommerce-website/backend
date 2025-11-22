package com.spring.fit.backend.product.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailWithPromotionResponse {
    @JsonProperty("detailId")
    private Long detailId;

    @JsonProperty("productId")
    private Long productId;

    private String title;
    private BigDecimal price;          // base price
    private BigDecimal finalPrice;     // after promotion
    private Integer percentOff;        // integer percent
    private Long promotionId;          // nullable
    private String promotionName;      // nullable

    @JsonProperty("activeColor")
    private String activeColor;

    @JsonProperty("activeSize")
    private String activeSize;

    private List<String> images;
    private List<String> colors;

    @JsonProperty("mapSizeToQuantity")
    private Map<String, Integer> mapSizeToQuantity;

    private List<String> description;
}


