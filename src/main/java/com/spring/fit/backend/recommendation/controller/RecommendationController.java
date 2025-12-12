package com.spring.fit.backend.recommendation.controller;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.product.domain.dto.response.ProductCardWithPromotionResponse;
import com.spring.fit.backend.recommendation.domain.dto.InteractionEventRequest;
import com.spring.fit.backend.recommendation.domain.dto.SizeRecommendationResponse;
import com.spring.fit.backend.recommendation.service.RecommendationService;
import com.spring.fit.backend.recommendation.service.SizeRecommendationService;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;
    private final SizeRecommendationService sizeRecommendationService;

    @PostMapping("/interactions")
    public ResponseEntity<Void> recordInteraction(@Valid @RequestBody InteractionEventRequest request) {
        Long userId = getCurrentUserId()
                .orElseThrow(() -> new ErrorException(HttpStatus.UNAUTHORIZED, "User must be authenticated"));
        
        log.info("Recording interaction: userId={}, productId={}, actionType={}, count={}", 
                 userId, request.getProductId(), request.getActionType(), request.getCount());
        
        recommendationService.recordInteraction(
                request.getActionType(),
                userId,
                request.getProductId(),
                request.getCount()
        );
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/for-you")
    public ResponseEntity<List<ProductCardWithPromotionResponse>> getRecommendationsForYou(
            @RequestParam(required = false) String sessionHistory,
            @RequestParam(defaultValue = "10") int limit) {
        
        Long userId = getCurrentUserId().orElse(null);
        List<Long> sessionHistoryList = parseSessionHistory(sessionHistory);
        
        List<ProductCardWithPromotionResponse> recommendations = recommendationService.getRecommendationsForYou(
                userId, sessionHistoryList, limit);
        
        return ResponseEntity.ok(recommendations);
    }

    private List<Long> parseSessionHistory(String sessionHistory) {
        if (sessionHistory == null || sessionHistory.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Arrays.stream(sessionHistory.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
        } catch (NumberFormatException e) {
            log.warn("Invalid sessionHistory format: {}", sessionHistory);
            return null;
        }
    }

    @GetMapping("/similar/{itemId}")
    public ResponseEntity<List<ProductCardWithPromotionResponse>> getSimilarItems(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "10") int limit) {
        
        Long userId = getCurrentUserId().orElse(null);
        
        log.info("Getting similar items for itemId: {}, userId: {}, limit: {}", itemId, userId, limit);
        
        List<ProductCardWithPromotionResponse> similarItems = recommendationService.getSimilarItems(
                itemId, userId, limit);
        
        return ResponseEntity.ok(similarItems);
    }

    @GetMapping("/size-recommendation/{productId}")
    public ResponseEntity<SizeRecommendationResponse> getSizeRecommendation(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "30") Integer similarUserLimit) {
        
        Long userId = getCurrentUserId()
                .orElseThrow(() -> new ErrorException(HttpStatus.UNAUTHORIZED, "User must be authenticated"));
        
        log.info("Getting size recommendation for user: {}, product: {}, similarUserLimit: {}", 
                userId, productId, similarUserLimit);
        
        SizeRecommendationResponse recommendation = sizeRecommendationService.getSizeRecommendation(
                userId, productId, similarUserLimit);
        
        return ResponseEntity.ok(recommendation);
    }

    @PostMapping("/admin/train-model")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> trainModel() {
        log.info("Manual model training requested");
        try {
            recommendationService.trainModel();
            return ResponseEntity.ok("Model training started successfully");
        } catch (Exception e) {
            log.error("Error during manual model training: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start model training: " + e.getMessage());
        }
    }

    private Optional<Long> getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null || 
                "anonymousUser".equals(authentication.getName())) {
                return Optional.empty();
            }
            
            String email = authentication.getName();
            UserEntity user = userRepository.findActiveUserByEmail(email)
                    .orElse(null);
            
            if (user == null) {
                return Optional.empty();
            }
            
            return Optional.of(user.getId());
        } catch (Exception e) {
            log.warn("Error getting current user ID: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
