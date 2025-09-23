package com.spring.fit.backend.product.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {
    
    @NotBlank(message = "Product title cannot be blank")
    @Size(max = 500, message = "Product title cannot exceed 500 characters")
    private String title;
    
    @Size(max = 2000, message = "Product description cannot exceed 2000 characters")
    private String description;
    
    @NotEmpty(message = "Product must belong to at least one category")
    private Set<@NotNull(message = "Category ID cannot be null") @Positive(message = "Category ID must be positive") Long> categoryIds;
}
