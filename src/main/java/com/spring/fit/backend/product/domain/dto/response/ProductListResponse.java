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
    private String thumbnail; // ảnh đầu tiên của 1 màu bất kì
    private Long categoryId; // tên category chính
    private List<ColorResponse> variantColors; // danh sách màu đang active
    private List<SizeResponse> variantSizes; // danh sách màu đang inactive
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}





