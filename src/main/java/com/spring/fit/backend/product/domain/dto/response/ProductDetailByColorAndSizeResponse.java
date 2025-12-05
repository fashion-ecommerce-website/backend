package com.spring.fit.backend.product.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailByColorAndSizeResponse {

    private Long detailId;
    private Long productId;
    private String title;
    private List<String> images;
    private List<ColorResponse> variantColors;
    private ColorResponse activeColor;
    private List<SizeResponse> variantSizes;
    private SizeResponse activeSize;
    private Integer quantity;
    private BigDecimal price;
}
