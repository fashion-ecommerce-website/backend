package com.spring.fit.backend.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.fit.backend.recommendation.dto.response.AIInterpretedRequirements;
import com.spring.fit.backend.weather.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service to interpret natural language fashion requests using AI.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NaturalLanguageRecommendationService {
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";
    
    @Value("${GEMINI_API_KEY:}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WeatherService weatherService;
    
    /**
     * Interprets user's natural language message and returns structured requirements.
     */
    public AIInterpretedRequirements interpretUserMessage(String message, String location) {
        
        // Check if user mentions weather-related concerns
        boolean needsWeather = detectsWeatherConcern(message);
        WeatherService.WeatherData weatherData = null;
        
        if (needsWeather && location != null && !location.isBlank()) {
            try {
                weatherData = weatherService.getWeatherForCity(location);
                if (weatherData != null) {
                    log.info("Retrieved weather data for location {}: {}°C, {}", 
                            location, weatherData.temperature(), weatherData.condition());
                } else {
                    log.warn("Weather data not available for location: {}", location);
                }
            } catch (Exception e) {
                log.warn("Failed to retrieve weather data for location {}: {}", location, e.getMessage());
            }
        }
        
        // Build prompt for Gemini AI
        String prompt = buildInterpretationPrompt(message, weatherData);
        
        try {
            String aiResponse = callGeminiAPI(prompt);
            log.info("Successfully interpreted message using AI");
            return parseAIResponse(aiResponse);
        } catch (Exception e) {
            // Silently fall back to rule-based interpretation
            // This is normal in dev env when quota is exceeded
            return buildFallbackInterpretation(message);
        }
    }
    
    /**
     * Detects if user's message mentions weather concerns.
     */
    private boolean detectsWeatherConcern(String message) {
        if (message == null) return false;
        
        String lowerMessage = message.toLowerCase();
        String[] weatherKeywords = {
            "weather", "hot", "cold", "rain", "sunny", "temperature",
            "humid", "dry", "sweat", "freeze", "warm", "cool", "climate"
        };
        
        for (String keyword : weatherKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Builds the prompt for Gemini AI to interpret user requirements.
     */
    private String buildInterpretationPrompt(String userMessage, WeatherService.WeatherData weather) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Analyze this fashion recommendation request and extract structured requirements.\n\n");
        prompt.append("User's message: \"").append(userMessage).append("\"\n\n");
        
        if (weather != null) {
            prompt.append("Current Weather Context:\n");
            prompt.append("- Temperature: ").append(weather.temperature()).append("°C\n");
            prompt.append("- Condition: ").append(weather.condition()).append("\n");
            prompt.append("- Weather Type: ").append(weather.weatherType()).append("\n");
            prompt.append("- Humidity: ").append(weather.humidity()).append("%\n");
            prompt.append("- Description: ").append(weather.description()).append("\n\n");
        }
        
        prompt.append("Please provide a JSON response with the following structure:\n");
        prompt.append("{\n");
        prompt.append("  \"categories\": [\"category1\"],      // Product category slugs e.g., [\"ao-thun\", \"quan-jogger\", \"giay-sneakers\"]\n");
        prompt.append("  \"styles\": [\"style1\", \"style2\"],  // e.g., [\"professional\", \"formal\", \"business\"]\n");
        prompt.append("  \"colors\": [\"color1\", \"color2\"],  // Recommended colors\n");
        prompt.append("  \"occasions\": [\"occasion1\"],       // e.g., [\"interview\", \"business\"]\n");
        prompt.append("  \"bodyTypes\": [\"any\"],             // Specific body types if mentioned, otherwise [\"any\"]\n");
        prompt.append("  \"weatherPreferences\": [\"pref1\"],  // e.g., [\"breathable\", \"lightweight\", \"warm\"]\n");
        prompt.append("  \"materials\": [\"material1\"],       // Recommended materials\n");
        prompt.append("  \"importanceScores\": {              // Weight 0-100 for each factor\n");
        prompt.append("    \"category\": 35,\n");
        prompt.append("    \"style\": 20,\n");
        prompt.append("    \"occasion\": 25,\n");
        prompt.append("    \"weather\": 15,\n");
        prompt.append("    \"color\": 10\n");
        prompt.append("  },\n");
        prompt.append("  \"weatherContext\": {\n");
        prompt.append("    \"temperature\": \"moderate\",     // hot/cold/moderate\n");
        prompt.append("    \"condition\": \"clear\",          // rainy/sunny/cloudy\n");
        prompt.append("    \"humidity\": \"normal\",          // high/normal/low\n");
        prompt.append("    \"needsWeatherData\": true,       // true if weather is a concern\n");
        prompt.append("    \"weatherAdvice\": \"advice text\" // Specific weather-based advice\n");
        prompt.append("  },\n");
        prompt.append("  \"contextSummary\": \"Brief summary of user's needs\",\n");
        prompt.append("  \"urgencyLevel\": 4                  // 1-5, based on importance mentioned\n");
        prompt.append("}\n\n");
        
        prompt.append("Available Product Categories:\n");
        prompt.append("Tops: ao-thun (t-shirts), ao-polo (polo), ao-so-mi (shirts), ao-hoodie (hoodies), ao-ni-sweatshirts (sweatshirts)\n");
        prompt.append("Bottoms: quan-jogger (joggers), quan-shorts (shorts), quan-dai (long pants)\n");
        prompt.append("Outerwear: ao-khoac (jackets), ao-khoac-phao (puffer), ao-khoac-cardigan (cardigan)\n");
        prompt.append("Footwear: giay-sneakers (sneakers), giay-sandals (sandals), dep-quai-ngang (slides)\n");
        prompt.append("Women's: dam (dresses), vay (skirts), do-boi (swimwear)\n");
        prompt.append("Accessories: mu-non (hats), non-bucket (bucket hats), non-bong-chay (caps), tui-vi (bags), balo (backpacks), vo (socks)\n\n");
        
        prompt.append("Guidelines:\n");
        prompt.append("- IMPORTANT: Map user requests to specific category slugs from the list above\n");
        prompt.append("- Extract all mentioned preferences (categories, styles, colors, occasions)\n");
        prompt.append("- Assign importance scores based on emphasis in the message\n");
        prompt.append("- If weather is mentioned or weather data is provided, factor it into recommendations\n");
        prompt.append("- Set higher urgency for important events (interviews, meetings, weddings)\n");
        prompt.append("- Be specific and practical in your recommendations\n");
        prompt.append("- Return ONLY the JSON response, no additional text\n");
        
        return prompt.toString();
    }
    
    /**
     * Calls Gemini AI API.
     */
    private String callGeminiAPI(String prompt) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured");
        }
        
        String url = String.format("%s?key=%s", GEMINI_API_URL, apiKey);
        
        Map<String, Object> requestBody = new HashMap<>();
        
        // Add generation config for JSON response
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topP", 0.9);
        generationConfig.put("maxOutputTokens", 2048);
        generationConfig.put("responseMimeType", "application/json");
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
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            }
            
            throw new RuntimeException("Failed to get response from Gemini API");
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            // Just throw without logging - will be logged once at higher level
            throw new RuntimeException("quota exceeded", e);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Gemini API client error ({}): {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("AI service error: " + e.getStatusCode(), e);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("Gemini API server error ({}): {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("AI service temporarily unavailable", e);
        }
    }
    
    /**
     * Extracts text from Gemini API response.
     */
    private String extractTextFromResponse(String responseBody) throws JsonProcessingException {
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
        
        if (candidates != null && !candidates.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            
            if (parts != null && !parts.isEmpty()) {
                return (String) parts.get(0).get("text");
            }
        }
        
        throw new RuntimeException("No valid response from Gemini API");
    }
    
    /**
     * Parses AI response into structured requirements.
     */
    private AIInterpretedRequirements parseAIResponse(String aiResponse) throws JsonProcessingException {
        // Clean the response (remove markdown code blocks if present)
        String cleanedResponse = aiResponse.trim();
        if (cleanedResponse.startsWith("```json")) {
            cleanedResponse = cleanedResponse.substring(7);
        }
        if (cleanedResponse.startsWith("```")) {
            cleanedResponse = cleanedResponse.substring(3);
        }
        if (cleanedResponse.endsWith("```")) {
            cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
        }
        cleanedResponse = cleanedResponse.trim();
        
        return objectMapper.readValue(cleanedResponse, AIInterpretedRequirements.class);
    }
    
    /**
     * Builds a fallback interpretation if AI fails.
     */
    private AIInterpretedRequirements buildFallbackInterpretation(String message) {
        // Removed duplicate log - already logged at higher level

        String lowerMessage = message == null ? "" : message.toLowerCase();

        // Basic keyword detection
        List<String> styles = new ArrayList<>();
        List<String> occasions = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        List<String> weatherPreferences = new ArrayList<>();
        List<String> bodyTypes = new ArrayList<>();

        // Detect categories from keywords
        categories.addAll(detectCategoriesFromMessage(lowerMessage));

        // Detect styles (handle common misspellings / variations)
        if (lowerMessage.contains("streetwear") || lowerMessage.contains("street wear") || lowerMessage.contains("streatwear") || lowerMessage.contains("street")) {
            styles.add("streetwear");
        }
        if (lowerMessage.contains("interview") || lowerMessage.contains("meeting") || lowerMessage.contains("business") 
                || lowerMessage.contains("work space") || lowerMessage.contains("workspace") || lowerMessage.contains("office")) {
            styles.add("professional");
            styles.add("formal");
            occasions.add("business");
            occasions.add("work");
        }
        if (lowerMessage.contains("party") || lowerMessage.contains("celebration")) {
            styles.add("elegant");
            occasions.add("party");
        }
        if (lowerMessage.contains("casual") || lowerMessage.contains("everyday") || lowerMessage.contains("hanging out") || lowerMessage.contains("hang out")) {
            styles.add("casual");
            occasions.add("casual");
        }
        if (lowerMessage.contains("gym") || lowerMessage.contains("workout") || lowerMessage.contains("sport")) {
            styles.add("sporty");
            occasions.add("gym");
        }
        if (lowerMessage.contains("formal") || lowerMessage.contains("wedding")) {
            styles.add("formal");
            occasions.add("formal");
        }

        // Detect colors
        String[] colorKeywords = {"black", "white", "blue", "red", "green", "yellow", "pink", "purple", "grey", "gray", "navy", "brown"};
        for (String color : colorKeywords) {
            if (lowerMessage.contains(color)) {
                colors.add(color);
            }
        }

        // Detect weather preferences
        if (lowerMessage.contains("hot") || lowerMessage.contains("summer") || lowerMessage.contains("warm")) {
            weatherPreferences.add("cool");
            weatherPreferences.add("breathable");
        }
        if (lowerMessage.contains("cold") || lowerMessage.contains("winter") || lowerMessage.contains("freeze")) {
            weatherPreferences.add("warm");
            weatherPreferences.add("insulated");
        }
        if (lowerMessage.contains("rain")) {
            weatherPreferences.add("waterproof");
        }

        // Parse height and weight if present and infer a simple body type
        try {
            java.util.regex.Matcher hm = java.util.regex.Pattern.compile("(height[s]?:?\\s*)(\\d{2,3})\\s*(cm)?").matcher(lowerMessage);
            java.util.regex.Matcher wm = java.util.regex.Pattern.compile("(weight[s]?:?\\s*)(\\d{2,3})\\s*(kg)?").matcher(lowerMessage);
            Integer heightCm = null;
            Integer weightKg = null;
            if (hm.find()) {
                heightCm = Integer.parseInt(hm.group(2));
            }
            if (wm.find()) {
                weightKg = Integer.parseInt(wm.group(2));
            }
            if (heightCm != null && weightKg != null && heightCm > 0) {
                double heightM = heightCm / 100.0;
                double bmi = weightKg / (heightM * heightM);
                if (bmi < 18.5) bodyTypes.add("slim");
                else if (bmi < 25) bodyTypes.add("average");
                else if (bmi < 30) bodyTypes.add("athletic");
                else bodyTypes.add("plus-size");
            }
        } catch (Exception ex) {
            // ignore parsing errors
        }

        if (bodyTypes.isEmpty()) {
            bodyTypes.add("any");
        }

        // Calculate importance scores with sensible defaults so fallback isn't completely zeroed
        Map<String, Double> importanceScores = new HashMap<>();
        importanceScores.put("category", categories.isEmpty() ? 0.0 : 35.0);
        importanceScores.put("style", styles.isEmpty() ? 10.0 : 20.0);
        importanceScores.put("occasion", occasions.isEmpty() ? 10.0 : 25.0);
        importanceScores.put("weather", weatherPreferences.isEmpty() ? 5.0 : 15.0);
        importanceScores.put("color", colors.isEmpty() ? 5.0 : 10.0);

        // Build context summary with detected info
        StringBuilder contextSummary = new StringBuilder("Rule-based interpretation");
        if (!categories.isEmpty()) {
            contextSummary.append(" | categories: ").append(String.join(", ", categories));
        }
        if (!styles.isEmpty()) {
            contextSummary.append(" | styles: ").append(String.join(", ", styles));
        }
        if (!bodyTypes.isEmpty() && !bodyTypes.contains("any")) {
            contextSummary.append(" | bodyType: ").append(String.join(", ", bodyTypes));
        }

        return AIInterpretedRequirements.builder()
                .categories(categories)
                .styles(styles.isEmpty() ? List.of() : styles)
                .colors(colors)
                .occasions(occasions.isEmpty() ? List.of() : occasions)
                .bodyTypes(bodyTypes)
                .weatherPreferences(weatherPreferences)
                .materials(List.of())
                .importanceScores(importanceScores)
                .contextSummary(contextSummary.toString())
                .urgencyLevel(3)
                .build();
    }
    
    /**
     * Detects product categories from user message keywords.
     */
    private List<String> detectCategoriesFromMessage(String message) {
        List<String> categories = new ArrayList<>();
        
        // Check for formal/business context first
        boolean isFormalContext = message.matches(".*(formal|interview|business|work ?space|workspace|office|professional|meeting).*");
        
        // Footwear
        if (message.matches(".*(shoe|shoes|sneaker|sneakers|footwear).*")) categories.add("giay-sneakers");
        if (message.matches(".*(sandal|sandals|slide|slides).*")) categories.add("giay-sandals");
        if (message.matches(".*(mule|mules).*")) categories.add("giay-mules");

        // Tops
        if (message.matches(".*(t-shirt|tshirt|tee|tees).*")) categories.add("ao-thun");
        if (message.matches(".*(polo).*")) categories.add("ao-polo");
        if (message.matches(".*(shirt|shirts|blouse|blouses).*") && !message.contains("t-shirt")) categories.add("ao-so-mi");
        if (message.matches(".*(hoodie|hoodies|hood).*")) categories.add("ao-hoodie");
        if (message.matches(".*(sweatshirt|sweatshirts|sweater|sweaters).*")) categories.add("ao-ni-sweatshirts");

        // Bottoms
        if (message.matches(".*(jogger|joggers|track ?pant|trackpants).*")) categories.add("quan-jogger");
        if (message.matches(".*(short|shorts).*")) categories.add("quan-shorts");
        if (message.matches(".*(pant|pants|trouser|trousers).*") && !message.matches(".*(short|shorts).*")) {
            // Prefer joggers when explicitly mentioned
            if (message.matches(".*(jogger|joggers).*")) categories.add("quan-jogger");
            else categories.add("quan-dai");
        }

        // Outerwear
        if (message.matches(".*(jacket|jackets|coat|coats).*")) {
            if (message.contains("puffer") || message.contains("down")) categories.add("ao-khoac-phao");
            else if (message.contains("cardigan")) categories.add("ao-khoac-cardigan");
            else categories.add("ao-khoac");
        }

        // Women's
        if (message.matches(".*(dress|dresses).*")) categories.add("dam");
        if (message.matches(".*(skirt|skirts).*")) categories.add("vay");
        if (message.matches(".*(swimwear|swimsuit|bikini).*")) categories.add("do-boi");

        // Accessories
        if (message.matches(".*(hat|hats|cap|caps).*")) {
            if (message.contains("bucket")) categories.add("non-bucket");
            else if (message.contains("baseball") || message.contains("snapback")) categories.add("non-bong-chay");
            else categories.add("mu-non");
        }
        if (message.matches(".*(bag|bags|backpack|backpacks|tote|totes).*")) {
            if (message.contains("tote")) categories.add("tui-tote");
            else if (message.contains("backpack")) categories.add("balo");
            else if (message.contains("crossbody")) categories.add("tui-deo-cheo");
            else categories.add("tui-vi");
        }
        if (message.matches(".*(sock|socks).*")) categories.add("vo");

        // Generic outfit - suggest common categories
        if (message.matches(".*(outfit|outfits|clothing|wear|clothes).*") && categories.isEmpty()) {
            // For formal contexts, prioritize shirts and dress pants
            if (isFormalContext) {
                categories.add("ao-so-mi");  // Formal shirts
                categories.add("quan-dai");  // Dress pants
            } else {
                categories.add("ao-thun");
                categories.add("quan-jogger");
            }
        }

        // Deduplicate while preserving order
        List<String> dedup = new ArrayList<>();
        for (String c : categories) if (!dedup.contains(c)) dedup.add(c);
        return dedup;
    }
}
