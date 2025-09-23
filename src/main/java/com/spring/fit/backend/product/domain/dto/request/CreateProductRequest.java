package com.spring.fit.backend.product.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {
    
    @NotBlank(message = "Product title cannot be blank")
    @Size(max = 500, message = "Product title cannot exceed 500 characters")
    private String title;
    
    @Size(max = 2000, message = "Product description cannot exceed 2000 characters")
    private String description;
    
    @NotEmpty(message = "Product must belong to at least one category")
    private Set<@NotNull(message = "Category ID cannot be null") @Positive(message = "Category ID must be positive") Long> categoryIds;
    
    @NotEmpty(message = "Product must have at least one variant")
    @Valid
    private List<ProductDetailRequest> productDetails;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDetailRequest {
        
        @NotNull(message = "Color ID cannot be null")
        @Positive(message = "Color ID must be positive")
        private Short colorId;
        
        @NotNull(message = "Size ID cannot be null") 
        @Positive(message = "Size ID must be positive")
        private Short sizeId;
        
        @NotNull(message = "Product price cannot be null")
        @Positive(message = "Product price must be greater than 0")
        private java.math.BigDecimal price;
        
        @NotNull(message = "Quantity cannot be null")
        @Min(value = 0, message = "Quantity cannot be negative")
        private Integer quantity;
    }
}
