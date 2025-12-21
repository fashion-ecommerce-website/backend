package com.spring.fit.backend.product.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListResponse {
    
    private Long id;
    private String title;
    private String description;
    private String thumbnail;
    private Long currentDetailId;
    private Long categoryId;
    private List<ColorResponse> variantColors;
    private List<SizeResponse> variantSizes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalQuantity;
    private Boolean hasOutOfStock;
}





