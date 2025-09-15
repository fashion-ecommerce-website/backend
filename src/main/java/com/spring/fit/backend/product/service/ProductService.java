package com.spring.fit.backend.product.service;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.product.domain.dto.ProductCardView;
import com.spring.fit.backend.product.domain.dto.ProductDetailResponse;

import java.util.List;

public interface ProductService {
    PageResult<ProductCardView> filterByCategory(
            String categorySlug,
            String title,
            List<String> colorNames,      // có thể null/empty
            List<String> sizeCodes,       // có thể null/empty
            String priceBucket,           // "<1m" | "1-2m" | "2-3m" | ">4m" | null
            String sortBy,                // "price_asc","price_desc","name_asc","name_desc"
            int page,
            int pageSize);
    
    List<ProductCardView> getRecentlyViewedProducts(List<Long> productIds);
    
    ProductDetailResponse getProductDetailById(Long detailId);

    ProductDetailResponse getProductDetailByColor(Long baseDetailId, String activeColor);
}
