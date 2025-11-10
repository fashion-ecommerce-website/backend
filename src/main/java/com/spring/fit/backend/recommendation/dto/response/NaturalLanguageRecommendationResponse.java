package com.spring.fit.backend.recommendation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for natural language recommendation requests.
 * Can either contain product recommendations or request more information from the user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaturalLanguageRecommendationResponse {
    
    // Status of the recommendation
    private RecommendationStatus status;
    
    // Product recommendations (if available)
    private List<ProductRecommendationResponse> recommendations;
    
    // Message to the user
    private String message;
    
    // Missing information that would help improve recommendations
    private List<String> suggestedQuestions;
    
    // Confidence score of the recommendations (0-100)
    private Double confidenceScore;
    
    // AI's interpretation summary
    private String interpretationSummary;
    
    public enum RecommendationStatus {
        SUCCESS,                    // Found good recommendations
        NEEDS_MORE_INFO,           // Need more information from user
        PARTIAL_RESULTS,           // Found some results but need more info for better ones
        NO_RESULTS                 // No matching products found
    }
}
