package com.spring.fit.backend.product.domain.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailPreviewResponse {
    private String color;
    private List<String> imageUrls;
    private String size;
    private Integer quantity;
    private BigDecimal price;

    // ⚠️ Cột đánh dấu lỗi validation
    private boolean isError = false;
    private String errorMessage; // thông tin lỗi chi tiết

    public ProductDetailPreviewResponse(String color, List<String> imageUrls, String size, Integer quantity, BigDecimal price) {
        this.color = color;
        this.imageUrls = imageUrls;
        this.size = size;
        this.quantity = quantity;
        this.price = price;
        this.isError = false;
        this.errorMessage = null;
    }
}
