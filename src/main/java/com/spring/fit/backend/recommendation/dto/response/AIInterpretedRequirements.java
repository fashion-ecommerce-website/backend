package com.spring.fit.backend.recommendation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI-interpreted requirements from user's natural language message.
 * This is the structured output from Gemini AI that interprets user needs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInterpretedRequirements {
    
    // Interpreted requirements
    private List<String> styles;           // e.g., ["professional", "formal", "business"]
    private List<String> colors;           // e.g., ["navy", "black", "white"]
    private List<String> occasions;        // e.g., ["interview", "business", "formal"]
    private List<String> bodyTypes;        // e.g., ["any"] or specific if mentioned
    private List<String> weatherPreferences; // e.g., ["breathable", "cool", "light"]
    private List<String> materials;        // e.g., ["cotton", "linen"]
    private List<String> categories;       // e.g., ["ao-thun", "quan-jogger", "giay-sneakers"]
    
    // Scoring weights (0-100) based on importance in user's message
    private Map<String, Double> importanceScores;
    
    // Weather information if applicable
    private WeatherContext weatherContext;
    
    // AI's understanding of the context
    private String contextSummary;
    
    // Urgency/importance level (1-5)
    private Integer urgencyLevel;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherContext {
        private String temperature;      // e.g., "hot", "cold", "moderate"
        private String condition;        // e.g., "rainy", "sunny", "cloudy"
        private String humidity;         // e.g., "high", "low"
        private Boolean needsWeatherData; // true if user mentioned weather concerns
        private String weatherAdvice;    // AI's advice based on weather
    }
}
