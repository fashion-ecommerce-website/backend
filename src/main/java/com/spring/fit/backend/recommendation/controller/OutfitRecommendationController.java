package com.spring.fit.backend.recommendation.controller;

import com.spring.fit.backend.recommendation.dto.request.NaturalLanguageRecommendationRequest;
import com.spring.fit.backend.recommendation.dto.request.RecommendationRequest;
import com.spring.fit.backend.recommendation.dto.response.NaturalLanguageRecommendationResponse;
import com.spring.fit.backend.recommendation.dto.response.ProductRecommendationResponse;
import com.spring.fit.backend.recommendation.service.OutfitRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class OutfitRecommendationController {

    private final OutfitRecommendationService recommendationService;

    @PostMapping
    public ResponseEntity<List<ProductRecommendationResponse>> getRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(recommendationService.getRecommendations(request));
    }

    @PostMapping("/natural-language")
    public ResponseEntity<NaturalLanguageRecommendationResponse> getRecommendationsFromNaturalLanguage(
            @Valid @RequestBody NaturalLanguageRecommendationRequest request) {
        return ResponseEntity.ok(recommendationService.getRecommendationsFromNaturalLanguage(request));
    }

    @PostMapping("/analyze/{productId}")
    public ResponseEntity<Void> analyzeProduct(@PathVariable Long productId) {
        recommendationService.analyzeAndSaveProductMetadata(productId);
        return ResponseEntity.ok().build();
    }
}
