package com.spring.fit.backend.product.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductDetailImagesRequest {

    // List of image URLs to delete
    private List<String> imagesToDelete;

    // Note: New images will be handled via MultipartFile in controller
}
