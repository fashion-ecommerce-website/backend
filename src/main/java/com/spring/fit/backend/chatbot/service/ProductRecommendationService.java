package com.spring.fit.backend.chatbot.service;

import com.spring.fit.backend.chatbot.domain.dto.ChatbotResponse;

import java.util.List;
import java.util.Set;

public interface ProductRecommendationService {
    Set<Long> extractProductIdsFromQuery(String query);
    List<ChatbotResponse.ProductRecommendation> getProductRecommendations(Set<Long> productIds);
    
    /**
     * Check if query is asking about products
     */
    boolean isProductRelatedQuery(String query);
    
    /**
     * Check if found products are relevant to the query
     */
    boolean checkProductsRelevance(String query, Set<Long> productIds);
    
    /**
     * Get list of available product categories
     */
    List<String> getAvailableProductCategories();
}


