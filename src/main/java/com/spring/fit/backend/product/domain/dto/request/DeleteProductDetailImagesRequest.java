package com.spring.fit.backend.product.domain.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteProductDetailImagesRequest {

    @NotEmpty(message = "Images to delete cannot be empty")
    private List<String> imageUrls;
}
