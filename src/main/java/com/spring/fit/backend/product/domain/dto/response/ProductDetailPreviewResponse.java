package com.spring.fit.backend.product.domain.dto.response;

import lombok.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailPreviewResponse {
    private String color;
    private List<String> imageUrls; // preview URL hoặc tên file tạm
    private String size;
    private Integer quantity;
    private BigDecimal price;

    // ⚠️ Cột đánh dấu lỗi validation
    private boolean isError = false;
    private String errorMessage; // thông tin lỗi chi tiết

    // NEW: giữ reference tới file local (ZIP) theo color
    private List<File> localFiles;

    public ProductDetailPreviewResponse(String color, List<String> imageUrls, String size, Integer quantity, BigDecimal price) {
        this.color = color;
        this.imageUrls = imageUrls;
        this.size = size;
        this.quantity = quantity;
        this.price = price;
        this.isError = false;
        this.errorMessage = null;
        this.localFiles = new ArrayList<>();
    }
}

