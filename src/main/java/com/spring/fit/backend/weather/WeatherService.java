package com.spring.fit.backend.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";
    
    @Value("${WEATHER_API_KEY:}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Get weather data by city name.
     * Supports formats: "City", "City,Country", "City, Country"
     */
    @Cacheable(value = "weather", key = "#cityName")
    public WeatherData getWeatherForCity(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            log.warn("City name is null or empty");
            return null;
        }
        
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Weather API key is not configured");
            return null;
        }
        
        try {
            // Clean and format city name for OpenWeatherMap API
            // For multi-word cities, try without spaces first (e.g., "Da Nang" -> "DaNang")
            // This format works better with OpenWeatherMap API
            String formattedCity = cityName.trim().replaceAll(",\\s+", ",");
            
            // If city has spaces before comma, try without spaces first
            String[] parts = formattedCity.split(",");
            if (parts[0].contains(" ")) {
                formattedCity = parts[0].replace(" ", "") + (parts.length > 1 ? "," + parts[1] : "");
            }
            
            String url = UriComponentsBuilder.fromUriString(WEATHER_API_URL)
                .queryParam("q", formattedCity)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "vi")
                .toUriString();
                
            log.debug("Fetching weather data for city: {}", formattedCity);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                
                // Extract weather data
                double temp = root.path("main").path("temp").asDouble();
                int humidity = root.path("main").path("humidity").asInt();
                String description = root.path("weather").get(0).path("description").asText();
                String condition = root.path("weather").get(0).path("main").asText().toLowerCase();
                String icon = root.path("weather").get(0).path("icon").asText();
                String location = root.path("name").asText();
                
                WeatherData weatherData = new WeatherData(
                    location,
                    Math.round(temp * 10.0) / 10.0, // Round to 1 decimal
                    humidity,
                    description,
                    condition,
                    icon,
                    getWeatherType(temp, condition)
                );
                
                log.info("Successfully fetched weather for {}: {}°C, {}", location, weatherData.temperature(), description);
                return weatherData;
            }
        } catch (RestClientException e) {
            log.error("Error calling weather API for city '{}': {}", cityName, e.getMessage());
            // Try alternative format if first attempt fails
            return tryAlternativeFormats(cityName);
        } catch (Exception e) {
            log.error("Error parsing weather data for city '{}': {}", cityName, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Try alternative city name formats if the first attempt fails.
     */
    private WeatherData tryAlternativeFormats(String cityName) {
        // Try multiple alternative formats in order of likelihood to succeed
        List<String> alternativeFormats = new java.util.ArrayList<>();
        
        // Extract just the city name (before comma)
        String[] parts = cityName.split(",");
        String cityPart = parts[0].trim();
        String countryCode = parts.length > 1 ? parts[1].trim() : null;
        
        // Priority order based on OpenWeatherMap API behavior:
        // 1. No spaces (most likely to work): "DaNang"
        String noSpace = cityPart.replace(" ", "");
        if (!noSpace.equals(cityPart) && !noSpace.isEmpty()) {
            alternativeFormats.add(noSpace);
            if (countryCode != null) {
                alternativeFormats.add(noSpace + "," + countryCode);
            }
        }
        
        // 2. Original city name with spaces: "Da Nang"
        if (!alternativeFormats.contains(cityPart)) {
            alternativeFormats.add(cityPart);
        }
        
        // 3. With country code if available: "Da Nang,VN"
        if (countryCode != null && !alternativeFormats.contains(cityPart + "," + countryCode)) {
            alternativeFormats.add(cityPart + "," + countryCode);
        }
        
        // Try each alternative format
        for (String altCity : alternativeFormats) {
            log.debug("Retrying with alternative format: {}", altCity);
            
            try {
                String url = UriComponentsBuilder.fromUriString(WEATHER_API_URL)
                    .queryParam("q", altCity)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .queryParam("lang", "vi")
                    .toUriString();
                    
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    
                    double temp = root.path("main").path("temp").asDouble();
                    int humidity = root.path("main").path("humidity").asInt();
                    String description = root.path("weather").get(0).path("description").asText();
                    String condition = root.path("weather").get(0).path("main").asText().toLowerCase();
                    String icon = root.path("weather").get(0).path("icon").asText();
                    String location = root.path("name").asText();
                    
                    WeatherData weatherData = new WeatherData(
                        location,
                        Math.round(temp * 10.0) / 10.0,
                        humidity,
                        description,
                        condition,
                        icon,
                        getWeatherType(temp, condition)
                    );
                    
                    log.info("Successfully fetched weather using alternative format '{}' for {}: {}°C, {}", 
                            altCity, location, weatherData.temperature(), description);
                    return weatherData;
                }
            } catch (Exception e) {
                log.debug("Alternative format '{}' failed: {}", altCity, e.getMessage());
            }
        }
        
        log.error("All alternative formats failed for city: {}", cityName);
        return null;
    }
    
    /**
     * Get weather data by coordinates (latitude, longitude).
     */
    @Cacheable(value = "weather", key = "'coords:' + #lat + ',' + #lon")
    public WeatherData getWeatherByCoordinates(double lat, double lon) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Weather API key is not configured");
            return null;
        }
        
        try {
            String url = UriComponentsBuilder.fromUriString(WEATHER_API_URL)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "vi")
                .toUriString();
                
            log.debug("Fetching weather data for coordinates: {}, {}", lat, lon);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                
                double temp = root.path("main").path("temp").asDouble();
                int humidity = root.path("main").path("humidity").asInt();
                String description = root.path("weather").get(0).path("description").asText();
                String condition = root.path("weather").get(0).path("main").asText().toLowerCase();
                String icon = root.path("weather").get(0).path("icon").asText();
                String location = root.path("name").asText();
                
                WeatherData weatherData = new WeatherData(
                    location,
                    Math.round(temp * 10.0) / 10.0,
                    humidity,
                    description,
                    condition,
                    icon,
                    getWeatherType(temp, condition)
                );
                
                log.info("Successfully fetched weather for {}: {}°C, {}", location, weatherData.temperature(), description);
                return weatherData;
            }
        } catch (Exception e) {
            log.error("Error fetching weather data by coordinates ({}, {}): {}", lat, lon, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Determine weather type based on temperature and condition.
     */
    private String getWeatherType(double tempCelsius, String condition) {
        if (condition.contains("rain") || condition.contains("drizzle") || condition.contains("thunder")) {
            return "rainy";
        } else if (tempCelsius < 10) {
            return "very_cold";
        } else if (tempCelsius < 20) {
            return "cold";
        } else if (tempCelsius < 30) {
            return "moderate";
        } else {
            return "hot";
        }
    }
    
    /**
     * Enhanced WeatherData record with more information.
     */
    public record WeatherData(
        String cityName,
        double temperature,        // Temperature in Celsius (rounded)
        int humidity,             // Humidity percentage
        String description,       // Weather description (e.g., "clear sky", "light rain")
        String condition,         // Weather condition (e.g., "clear", "rain", "clouds")
        String icon,             // Weather icon code
        String weatherType       // Simplified type for recommendations
    ) {
        /**
         * Check if this weather is suitable for a given type.
         */
        public boolean isSuitableFor(String weatherType) {
            return this.weatherType.equalsIgnoreCase(weatherType);
        }
        
        /**
         * Get fashion recommendation attributes based on weather.
         */
        public List<String> getRecommendedAttributes() {
            return switch (weatherType) {
                case "very_cold" -> List.of("winter", "warm", "insulated", "layered");
                case "cold" -> List.of("warm", "layered", "jacket", "long-sleeve");
                case "moderate" -> List.of("light", "breathable", "versatile", "comfortable");
                case "hot" -> List.of("lightweight", "summer", "breathable", "short-sleeve");
                case "rainy" -> List.of("waterproof", "raincoat", "umbrella", "water-resistant");
                default -> List.of("all-weather", "versatile");
            };
        }
        
        /**
         * Get a human-readable weather summary.
         */
        public String getSummary() {
            return String.format("%s: %.1f°C, %s (Humidity: %d%%)", 
                cityName, temperature, description, humidity);
        }
        
        /**
         * Check if weather is considered comfortable.
         */
        public boolean isComfortable() {
            return temperature >= 18 && temperature <= 28 && 
                   humidity >= 30 && humidity <= 70 &&
                   !condition.contains("rain") && !condition.contains("storm");
        }
    }
}
