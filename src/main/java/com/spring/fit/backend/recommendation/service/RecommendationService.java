package com.spring.fit.backend.recommendation.service;

import com.spring.fit.backend.product.domain.dto.response.ProductCardWithPromotionResponse;
import com.spring.fit.backend.recommendation.domain.dto.InteractionEventRequest;
import com.spring.fit.backend.recommendation.domain.enums.ActionType;

import java.util.List;

public interface RecommendationService {

    void recordInteraction(ActionType actionType, Long userId, Long productId, Integer count);

    List<ProductCardWithPromotionResponse> getRecommendationsForYou(Long userId, List<Long> sessionHistory, int limit);

    List<ProductCardWithPromotionResponse> getSimilarItems(Long itemId, Long userId, int limit);

    void trainModel();
}
