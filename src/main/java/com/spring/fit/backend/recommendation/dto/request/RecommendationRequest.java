package com.spring.fit.backend.recommendation.dto.request;

import com.spring.fit.backend.recommendation.domain.enums.BodyType;
import com.spring.fit.backend.recommendation.domain.enums.Occasion;
import com.spring.fit.backend.recommendation.domain.enums.Season;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for getting fashion recommendations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    @NotNull(message = "User ID is required")
    private String userId;
    
    private BodyType bodyType;
    
    @NotNull(message = "Location is required for weather-based recommendations")
    private String location;
    
    // Preferences
    private List<String> preferredStyles;
    private List<String> preferredColors;
    private List<String> preferredMaterials;
    
    // Context
    private Occasion occasion;
    private Season season;
    
    // Pagination
    @Builder.Default
    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private int page = 0;
    
    @Builder.Default
    @Min(value = 1, message = "Limit must be greater than 0")
    @Max(value = 50, message = "Maximum limit is 50")
    private int limit = 10;
    
    // Filters
    private Double maxPrice;
    private List<Long> categoryIds;
    
    /**
     * Checks if the request contains weather information.
     * @return true if location is not null and not empty, false otherwise
     */
    public boolean hasWeatherInfo() {
        return location != null && !location.trim().isEmpty();
    }
    
    /**
     * Checks if the request contains any user preferences.
     * @return true if any of the preference lists is not null and not empty
     */
    public boolean hasUserPreferences() {
        return (preferredStyles != null && !preferredStyles.isEmpty()) ||
               (preferredColors != null && !preferredColors.isEmpty()) ||
               (preferredMaterials != null && !preferredMaterials.isEmpty());
    }
}