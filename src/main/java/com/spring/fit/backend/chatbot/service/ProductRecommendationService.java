package com.spring.fit.backend.chatbot.service;

import com.spring.fit.backend.chatbot.domain.dto.ChatbotResponse;

import java.util.List;
import java.util.Set;

public interface ProductRecommendationService {
    Set<Long> extractProductIdsFromQuery(String query);
    List<ChatbotResponse.ProductRecommendation> getProductRecommendations(Set<Long> productIds);
}


