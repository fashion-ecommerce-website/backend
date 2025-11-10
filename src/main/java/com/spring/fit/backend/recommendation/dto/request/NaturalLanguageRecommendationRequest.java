package com.spring.fit.backend.recommendation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Natural language request for AI-powered fashion recommendations.
 * Users describe their needs in natural language and the system uses AI to interpret requirements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaturalLanguageRecommendationRequest {
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Message is required")
    private String message; // Natural language input from user
    
    // Optional: User's location for weather context
    private String location;
    
    // Pagination
    @Builder.Default
    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private int page = 0;
    
    @Builder.Default
    @Min(value = 1, message = "Limit must be greater than 0")
    @Max(value = 50, message = "Maximum limit is 50")
    private int limit = 10;
    
    // Optional filters
    private Double maxPrice;
    private List<Long> categoryIds;
}
