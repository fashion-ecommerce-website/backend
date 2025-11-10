package com.spring.fit.backend.recommendation.dto.response;

import com.spring.fit.backend.product.domain.dto.response.ProductCardView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendationResponse implements ProductCardView {
    private Long detailId;
    private String productTitle;
    private String productSlug;
    private String colorName;
    private BigDecimal price;
    private Integer quantity;
    private List<String> colors;
    private List<String> imageUrls;
    private double matchScore;
    private List<String> matchingAttributes;
    
    // Implement methods from ProductCardView interface
    @Override
    public Long getDetailId() {
        return detailId;
    }

    @Override
    public String getProductTitle() {
        return productTitle;
    }

    @Override
    public String getProductSlug() {
        return productSlug;
    }

    @Override
    public String getColorName() {
        return colorName;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public Integer getQuantity() {
        return quantity;
    }

    @Override
    public List<String> getColors() {
        return colors;
    }

    @Override
    public List<String> getImageUrls() {
        return imageUrls;
    }
}
