package com.spring.fit.backend.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.fit.backend.ai.model.ProductAnalysis;
import com.spring.fit.backend.product.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAIService {
    // Updated to use gemini-2.5-flash which is available in v1beta API
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    
    @Value("${GEMINI_API_KEY}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public ProductAnalysis analyzeProduct(Product product) {
        String prompt = String.format("""
            Analyze this fashion product and provide details in JSON format:
            {
                "styles": ["casual", "formal", "sporty", ...],
                "suitableForBodyTypes": ["hourglass", "pear", "apple", ...],
                "suitableForWeather": ["hot", "cold", "rainy", ...],
                "occasions": ["work", "party", "sports", ...],
                "analysis": "Detailed analysis of the product..."
            }
            
            Product details:
            - Title: %s
            - Description: %s
            - Categories: %s
            
            Be concise and use relevant fashion terminology.
            """, 
            product.getTitle(), 
            product.getDescription() != null ? product.getDescription() : "",
            product.getCategories() != null ? product.getCategories().toString() : ""
        );
        
        try {
            if (apiKey == null || apiKey.isBlank()) {
                // Return mock response if API key is not configured
                return ProductAnalysis.builder()
                        .styles(List.of("casual", "sporty"))
                        .suitableForBodyTypes(List.of("HOURGLASS", "RECTANGLE"))
                        .suitableForWeather(List.of("MILD", "WARM"))
                        .occasions(List.of("CASUAL", "SPORT"))
                        .analysis("This is a sample analysis for " + product.getTitle())
                        .build();
            }
            
            String url = String.format("%s?key=%s", GEMINI_API_URL, apiKey);
            
            // Build request body with generation config
            Map<String, Object> requestBody = new HashMap<>();
            
            // Add generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7); // Tăng tính sáng tạo nhưng vẫn ổn định
            generationConfig.put("topP", 0.9);
            generationConfig.put("maxOutputTokens", 2048);
            requestBody.put("generationConfig", generationConfig);
            
            // Add content
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                    if (responseMap != null) {
                        String text = extractTextFromResponse(responseMap);
                        return parseAnalysis(text);
                    }
                } catch (JsonProcessingException e) {
                    log.error("Error parsing Gemini API response", e);
                }
            }
        } catch (Exception e) {
            log.error("Error analyzing product with Gemini AI", e);
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                if (candidate != null) {
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    if (content != null) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            Map<String, Object> part = parts.get(0);
                            if (part != null) {
                                Object text = part.get("text");
                                return text != null ? text.toString() : "";
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting text from Gemini response", e);
        }
        return "";
    }
    
    private ProductAnalysis parseAnalysis(String text) {
        try {
            // Extract JSON from markdown code blocks if present
            String jsonStr = text.replaceAll("```(json)?\\s*([\\s\\S]*?)\\s*```", "$2");
            return objectMapper.readValue(jsonStr, ProductAnalysis.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing AI analysis. Text was: " + text, e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error parsing AI analysis: " + e.getMessage(), e);
            return null;
        }
    }
    
}
