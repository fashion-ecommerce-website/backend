package com.spring.fit.backend.product.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public interface ProductCardView {
    Long getDetailId();
    String getProductTitle();
    String getProductSlug();
    String getColorName();
    BigDecimal getPrice();
    Integer getQuantity();

    List<String> getColors();

    List<String> getImageUrls();
}
