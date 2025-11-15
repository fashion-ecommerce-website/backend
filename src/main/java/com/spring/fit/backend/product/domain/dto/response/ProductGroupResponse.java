package com.spring.fit.backend.product.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductGroupResponse {
    private String category;
    private String productTitle;
    private String description;
    private List<ProductDetailPreviewResponse> productDetails = new ArrayList<>();

    public ProductGroupResponse(String productTitle, String description,String category) {
        this.productTitle = productTitle;
        this.description = description;
        this.category = category;
    }

    public void addDetail(ProductDetailPreviewResponse detail) {
        this.productDetails.add(detail);
    }
}

