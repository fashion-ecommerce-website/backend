package com.spring.fit.backend.recommendation.dto.response;

import com.spring.fit.backend.product.domain.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductRecommendation {
    private Product product;
    private double matchScore;
    private List<String> matchingAttributes;
}
