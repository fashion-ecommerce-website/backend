package com.spring.fit.backend.product.domain.dto.response;

import com.spring.fit.backend.category.domain.dto.CategoryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    
    private Long id;
    private String title;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<CategoryResponse> categories;
    private List<ProductDetailResponse> productDetails;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDetailResponse {
        private Long id;
        private String slug;
        private ColorResponse color;
        private SizeResponse size;
        private BigDecimal price;
        private Integer quantity;
        private Boolean isActive;
        private List<String> imageUrls;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColorResponse {
        private Short id;
        private String name;
        private String hex;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SizeResponse {
        private Short id;
        private String code;
        private String label;
    }
}






