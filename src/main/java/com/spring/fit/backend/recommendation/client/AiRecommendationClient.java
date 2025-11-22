package com.spring.fit.backend.recommendation.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Component
@Slf4j
public class AiRecommendationClient {

    private final RestTemplate restTemplate;
    private final String aiServiceBaseUrl;

    public AiRecommendationClient(
            RestTemplate restTemplate,
            @Value("${ai.service.base-url:http://localhost:5000}") String aiServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.aiServiceBaseUrl = aiServiceBaseUrl;
    }

    public List<Long> getUserRecommendations(Long userId) {
        try {
            String url = String.format("%s/recs/user/%d", aiServiceBaseUrl, userId);
            log.debug("Calling AI service: GET {}", url);
            
            Long[] response = restTemplate.getForObject(url, Long[].class);
            
            if (response != null && response.length > 0) {
                List<Long> result = Arrays.asList(response);
                log.info("Got {} recommendations for user_id: {}", result.size(), userId);
                return result;
            }
            
            return Collections.emptyList();
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("No recommendations found for user_id: {} (404)", userId);
            return null; // Trả về null để phân biệt với empty list
        } catch (Exception e) {
            log.error("Error calling AI service for user_id {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get user recommendations", e);
        }
    }

    public List<Long> getSimilarItems(Long itemId, Integer limit) {
        try {
            String url = String.format("%s/recs/item/%d?limit=%d", 
                                      aiServiceBaseUrl, itemId, limit != null ? limit : 10);
            log.debug("Calling AI service: GET {}", url);
            
            Long[] response = restTemplate.getForObject(url, Long[].class);
            
            if (response != null && response.length > 0) {
                List<Long> result = Arrays.asList(response);
                log.info("Got {} similar items for item_id: {}", result.size(), itemId);
                return result;
            }
            
            return Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error calling AI service for item_id {}: {}", itemId, e.getMessage());
            throw new RuntimeException("Failed to get similar items", e);
        }
    }

    public List<Long> getMostPopular(Integer limit) {
        try {
            String url = String.format("%s/recs/global/most-popular?limit=%d", 
                                      aiServiceBaseUrl, limit != null ? limit : 10);
            log.debug("Calling AI service: GET {}", url);
            
            Long[] response = restTemplate.getForObject(url, Long[].class);
            
            if (response != null && response.length > 0) {
                List<Long> result = Arrays.asList(response);
                log.info("Got {} popular items", result.size());
                return result;
            }
            
            return Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error calling AI service for popular items: {}", e.getMessage());
            throw new RuntimeException("Failed to get popular items", e);
        }
    }

    public void trainModel() {
        try {
            String url = String.format("%s/admin/retrain-model", aiServiceBaseUrl);
            log.info("Triggering model training: POST {}", url);
            
            restTemplate.postForObject(url, null, Object.class);
            
            log.info("Model training request sent successfully");
            
        } catch (Exception e) {
            log.error("Error triggering model training: {}", e.getMessage());
            throw new RuntimeException("Failed to trigger model training", e);
        }
    }
}

