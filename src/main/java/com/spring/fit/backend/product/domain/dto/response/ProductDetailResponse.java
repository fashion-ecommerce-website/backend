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
public class ProductDetailResponse {
    @JsonProperty("detailId")
    private Long detailId;
    
    private String title;
    private BigDecimal price;
    
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
