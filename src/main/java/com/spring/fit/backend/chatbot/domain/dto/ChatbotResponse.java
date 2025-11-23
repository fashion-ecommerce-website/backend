package com.spring.fit.backend.chatbot.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatbotResponse {
    private String type;
    private String message;
    private List<ProductRecommendation> recommendations;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductRecommendation {
        private Long objectId;  // productDetailId
        private String title;   // productTitle
        private String description; // description from product
        private String imageUrl; // product detail image
        private String color;    // product detail color
        private String size;     // product detail size
        private BigDecimal price; // product detail price
        private Integer quantity; // product detail quantity
    }
}

