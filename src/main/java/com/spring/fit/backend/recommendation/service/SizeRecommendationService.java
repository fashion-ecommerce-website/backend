package com.spring.fit.backend.recommendation.service;

import com.spring.fit.backend.recommendation.domain.dto.SizeRecommendationResponse;

public interface SizeRecommendationService {
    
    /**
     * Get size recommendation for a product based on user measurements
     * 
     * @param userId User ID
     * @param productId Product ID
     * @param similarUserLimit Maximum number of similar users to consider
     * @return Size recommendation response
     */
    SizeRecommendationResponse getSizeRecommendation(Long userId, Long productId, Integer similarUserLimit);
}
