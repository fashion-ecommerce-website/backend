package com.spring.fit.backend.product.domain.dto.request;

import com.spring.fit.backend.product.domain.dto.response.ProductDetailCheckItem;
import com.spring.fit.backend.product.domain.dto.response.ProductDetailPreviewResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailCheckRequest {
    private String productTitle; // product title của detail đang check
    private ProductDetailCheckItem detail; // color, size của detail đang check
    private List<ProductDetailCheckItem> fileProductDetails;
}
