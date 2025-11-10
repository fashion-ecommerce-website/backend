package com.spring.fit.backend.recommendation.service;
import com.spring.fit.backend.product.domain.entity.ProductImage;
import com.spring.fit.backend.product.domain.entity.Image;
import java.util.Objects;
import java.util.stream.Collectors;

import com.spring.fit.backend.ai.GeminiAIService;
import com.spring.fit.backend.ai.model.ProductAnalysis;
import com.spring.fit.backend.product.domain.entity.Product;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import java.math.BigDecimal;
import com.spring.fit.backend.product.repository.ProductMainRepository;
import com.spring.fit.backend.recommendation.domain.entity.ProductMetadata;
import com.spring.fit.backend.recommendation.dto.request.RecommendationRequest;
import com.spring.fit.backend.recommendation.dto.response.ProductRecommendationResponse;
import com.spring.fit.backend.recommendation.repository.ProductMetadataRepository;
import com.spring.fit.backend.product.domain.entity.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutfitRecommendationService {
    private final ProductMainRepository productRepository;
    private final ProductMetadataRepository metadataRepository;
    private final GeminiAIService geminiAIService;
    private final NaturalLanguageRecommendationService naturalLanguageService;

    @Transactional
    public void analyzeAndSaveProductMetadata(Long productId) {
        if (productId == null) {
            log.warn("Product ID cannot be null");
            return;
        }

        try {
            // Get the main product with details fetched
            var productOpt = productRepository.findActiveProductByIdWithDetails(productId);
            if (productOpt.isEmpty()) {
                log.warn("Product {} not found", productId);
                return;
            }

            var product = productOpt.get();

            // Get or create metadata
            var metadata = metadataRepository.findByProductId(productId)
                    .orElseGet(() -> {
                        var newMetadata = new ProductMetadata();
                        newMetadata.setProductId(product.getId());
                        return newMetadata;
                    });

            // Initialize null collections
            metadata.initializeNullCollections();

            // Process product details if available
            if (product.getDetails() != null && !product.getDetails().isEmpty()) {
                // Get the first detail for analysis
                ProductDetail firstDetail = product.getDetails().iterator().next();
                // Store the product ID in metadata
                metadata.setProductId(product.getId());
                
                // Update metadata with details from the first product detail if needed
                Color color = firstDetail.getColor();
                if (color != null && color.getName() != null) {
                    // Ensure colors list is mutable
                    if (metadata.getColors() == null) {
                        metadata.setColors(new ArrayList<>());
                    }
                    if (!metadata.getColors().contains(firstDetail.getColor().getName())) {
                        metadata.getColors().add(firstDetail.getColor().getName());
                    }
                }
            }

            try {
                // Call Gemini AI for analysis
                ProductAnalysis analysis = geminiAIService.analyzeProduct(product);

                if (analysis != null) {
                    // Update metadata with analysis results
                    if (analysis.getStyles() != null) {
                        metadata.setStyles(new ArrayList<>(analysis.getStyles()));
                    }
                    if (analysis.getSuitableForBodyTypes() != null) {
                        metadata.setSuitableForBodyTypes(new ArrayList<>(analysis.getSuitableForBodyTypes()));
                    }
                    if (analysis.getSuitableForWeather() != null) {
                        metadata.setSuitableForWeather(new ArrayList<>(analysis.getSuitableForWeather()));
                    }
                    if (analysis.getOccasions() != null) {
                        metadata.setOccasions(new ArrayList<>(analysis.getOccasions()));
                    }

                    metadata.setAiAnalysis(analysis.getAnalysis());
                    metadata.setLastAnalyzedAt(LocalDateTime.now());
                    metadata.setIsAnalyzed(true);

                    // Save the updated metadata
                    metadataRepository.save(metadata);
                    log.info("Successfully analyzed and saved metadata for product {}", productId);
                } else {
                    log.warn("No analysis results returned for product {}", productId);
                }
            } catch (Exception e) {
                log.error("Error analyzing product " + productId, e);
                throw new RuntimeException("Failed to analyze product: " + e.getMessage(), e);
            }
            log.info("Saved enhanced metadata for product {}", productId);

        } catch (Exception e) {
            log.error("Error analyzing product {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to analyze product", e);
        }
    }

    @Cacheable(value = "recommendations", key = "{#request.hashCode(), #request.page, #request.limit}")
    @Transactional(readOnly = true)
    public List<ProductRecommendationResponse> getRecommendations(RecommendationRequest request) {
        // Validate request
        if (request == null) {
            throw new IllegalArgumentException("Recommendation request cannot be null");
        }
        
        // Set default values
        if (request.getPage() < 0) request.setPage(0);
        if (request.getLimit() <= 0) request.setLimit(10);
        if (request.getLimit() > 50) request.setLimit(50);
        
        log.info("Generating recommendations for request: {}", request);
        
        // Get a limited number of products for better performance
        // Use findAllWithDetails to eagerly fetch the details collection
        List<Product> products = productRepository.findAllWithDetails(
            PageRequest.of(0, Math.min(100, request.getLimit() * 2)))
            .getContent();
        
        // Process products to get their IDs
        List<Long> productIds = products.stream()
            .map(Product::getId)
            .collect(Collectors.toList());
        
        log.debug("Querying metadata for {} products", productIds.size());
        
        // Get all metadata in one query
        List<ProductMetadata> allMetadata = metadataRepository.findByProductIdIn(productIds);
        log.info("Found {} analyzed metadata records out of {} products", allMetadata.size(), productIds.size());
        
        Map<Long, ProductMetadata> metadataMap = allMetadata.stream()
            .collect(Collectors.toMap(ProductMetadata::getProductId, m -> m, (m1, m2) -> m1));
        
        // Process products sequentially to avoid lazy loading issues in parallel streams
        return products.stream()
            .map(product -> {
                try {
                    // Get metadata from map
                    ProductMetadata metadata = metadataMap.get(product.getId());
                    
                    if (metadata == null || !Boolean.TRUE.equals(metadata.getIsAnalyzed())) {
                        log.debug("No valid metadata found for product {}", product.getId());
                        return null;
                    }
                    
                    // Initialize collections to prevent NPE
                    metadata.initializeNullCollections();
                    
                    // Calculate match score and get matching attributes
                    double score = calculateMatchScore(metadata, request);
                    if (score <= 0) {
                        log.debug("Product {} has score <= 0, skipping", product.getId());
                        return null;
                    }
                    List<String> matchingAttributes = getMatchingAttributes(metadata, request);
                    return mapToResponse(product, score, matchingAttributes);
                } catch (Exception e) {
                    log.error("Error processing product {}: {}", product.getId(), e.getMessage(), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(ProductRecommendationResponse::getMatchScore).reversed())
            .limit(Math.max(1, Math.min(50, request.getLimit()))) // Ensure limit is between 1 and 50
            .collect(Collectors.toList());
    }

    private double calculateMatchScore(ProductMetadata metadata, RecommendationRequest request) {
        if (metadata == null || request == null) {
            return 0.0;
        }

        double score = 0.0;

        // Body type matching (weight: 30%)
        if (request.getBodyType() != null && metadata.getSuitableForBodyTypes() != null) {
            boolean matches = metadata.getSuitableForBodyTypes().stream()
                    .anyMatch(bodyType -> bodyType != null && bodyType.equalsIgnoreCase(request.getBodyType().name()));
            if (matches) {
                score += 30.0;
            }
        }

        // Style matching (weight: 25%)
        if (request.getPreferredStyles() != null && !request.getPreferredStyles().isEmpty()
                && metadata.getStyles() != null && !metadata.getStyles().isEmpty()) {
            boolean matches = request.getPreferredStyles().stream()
                    .anyMatch(style -> metadata.getStyles().stream()
                            .anyMatch(s -> s != null && s.equalsIgnoreCase(style)));
            if (matches) {
                score += 25.0;
            }
        }

        // Occasion matching (weight: 20%)
        if (request.getOccasion() != null && metadata.getOccasions() != null) {
            boolean matches = metadata.getOccasions().stream()
                    .anyMatch(occasion -> occasion != null && occasion.equalsIgnoreCase(request.getOccasion().name()));
            if (matches) {
                score += 20.0;
            }
        }

        // Season matching (weight: 15%)
        if (request.getSeason() != null && metadata.getSeasons() != null) {
            boolean matches = metadata.getSeasons().stream()
                    .anyMatch(season -> season != null && season.equalsIgnoreCase(request.getSeason().name()));
            if (matches) {
                score += 15.0;
            }
        }

        // Color preference matching (weight: 10%)
        if (request.getPreferredColors() != null && !request.getPreferredColors().isEmpty()
                && metadata.getColors() != null && !metadata.getColors().isEmpty()) {
            boolean matches = request.getPreferredColors().stream()
                    .anyMatch(color -> metadata.getColors().stream()
                            .anyMatch(c -> c != null && c.equalsIgnoreCase(color)));
            if (matches) {
                score += 10.0;
            }
        }

        // Base score for products with analyzed metadata
        if (Boolean.TRUE.equals(metadata.getIsAnalyzed())) {
            score += 5.0;
        }

        // Normalize score to be between 0 and 100
        return Math.min(100.0, score);
    }

    private List<String> getMatchingAttributes(ProductMetadata metadata, RecommendationRequest request) {
        List<String> matchingAttributes = new ArrayList<>();
        
        if (metadata == null || request == null) {
            return matchingAttributes;
        }
        if (request.getBodyType() != null && metadata.getSuitableForBodyTypes() != null) {
            metadata.getSuitableForBodyTypes().stream()
                    .filter(bodyType -> bodyType != null && bodyType.equalsIgnoreCase(request.getBodyType().name()))
                    .findFirst()
                    .ifPresent(matchingAttributes::add);
        }

        // Add matching styles
        if (request.getPreferredStyles() != null && !request.getPreferredStyles().isEmpty()
                && metadata.getStyles() != null) {
            request.getPreferredStyles().stream()
                    .filter(style -> metadata.getStyles().stream()
                            .anyMatch(s -> s != null && s.equalsIgnoreCase(style)))
                    .findFirst()
                    .ifPresent(matchingAttributes::add);
        }

        // Add matching occasions
        if (request.getOccasion() != null && metadata.getOccasions() != null) {
            metadata.getOccasions().stream()
                    .filter(occasion -> occasion != null && occasion.equalsIgnoreCase(request.getOccasion().name()))
                    .findFirst()
                    .ifPresent(matchingAttributes::add);
        }

        // Add matching seasons
        if (request.getSeason() != null && metadata.getSeasons() != null) {
            metadata.getSeasons().stream()
                    .filter(season -> season != null && season.equalsIgnoreCase(request.getSeason().name()))
                    .findFirst()
                    .ifPresent(matchingAttributes::add);
        }

        // Add matching colors
        if (request.getPreferredColors() != null && !request.getPreferredColors().isEmpty()
                && metadata.getColors() != null) {
            request.getPreferredColors().stream()
                    .filter(color -> metadata.getColors().stream()
                            .anyMatch(c -> c != null && c.equalsIgnoreCase(color)))
                    .findFirst()
                    .ifPresent(matchingAttributes::add);
        }

        return matchingAttributes;
    }

    private ProductRecommendationResponse mapToResponse(Product product, double score,
            List<String> matchingAttributes) {
        // Get the first detail for price and image
        var firstDetail = product.getDetails() != null && !product.getDetails().isEmpty() 
            ? product.getDetails().iterator().next() 
            : null;

        // Initialize default values
        List<String> imageUrls = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        String colorName = null;
        BigDecimal price = null;
        Integer quantity = 0;
        Long detailId = null;
        String slug = null;

        if (firstDetail != null) {
            // Get image URLs if available
            if (firstDetail.getProductImages() != null) {
                imageUrls = firstDetail.getProductImages().stream()
                    .map(ProductImage::getImage)
                    .filter(Objects::nonNull)
                    .map(Image::getUrl)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            }

            // Get color if available
            if (firstDetail.getColor() != null) {
                colorName = firstDetail.getColor().getName();
                colors.add(colorName);
            }
            
            // Set other fields from the detail
            price = firstDetail.getPrice();
            quantity = firstDetail.getQuantity() != null ? firstDetail.getQuantity() : 0;
            detailId = firstDetail.getId();
            slug = firstDetail.getSlug();
        }

// Build the response
        return ProductRecommendationResponse.builder()
                .detailId(detailId)
                .productTitle(product.getTitle())
                .productSlug(slug)
                .colorName(colorName)
                .price(price)
                .quantity(quantity)
                .matchScore(score)
                .matchingAttributes(matchingAttributes != null ? matchingAttributes : List.of())
                .imageUrls(imageUrls)
                .colors(colors)
                .build();
    }
    
    /**
     * AI-Powered Natural Language Recommendation System.
     * Interprets user's message and provides recommendations based on AI understanding.
     */
    @Transactional(readOnly = true)
    public com.spring.fit.backend.recommendation.dto.response.NaturalLanguageRecommendationResponse getRecommendationsFromNaturalLanguage(
            com.spring.fit.backend.recommendation.dto.request.NaturalLanguageRecommendationRequest request) {
        
        log.info("Processing natural language recommendation request from user: {}", request.getUserId());
        log.info("User message: {}", request.getMessage());
        
        // Step 1: Use AI to interpret the user's message
        com.spring.fit.backend.recommendation.dto.response.AIInterpretedRequirements aiRequirements = 
            naturalLanguageService.interpretUserMessage(request.getMessage(), request.getLocation());
        
        log.info("AI interpreted requirements: {}", aiRequirements.getContextSummary());
        log.info("Importance scores: {}", aiRequirements.getImportanceScores());
        
        // Step 2: Check if we have enough information to make good recommendations
        InformationSufficiencyResult sufficiencyResult = checkInformationSufficiency(aiRequirements);
        
        if (!sufficiencyResult.isSufficient()) {
            log.info("Insufficient information detected. Confidence: {}%", sufficiencyResult.getConfidenceScore());
            return com.spring.fit.backend.recommendation.dto.response.NaturalLanguageRecommendationResponse.builder()
                    .status(com.spring.fit.backend.recommendation.dto.response.NaturalLanguageRecommendationResponse.RecommendationStatus.NEEDS_MORE_INFO)
                    .message(sufficiencyResult.getMessage())
                    .suggestedQuestions(sufficiencyResult.getSuggestedQuestions())
                    .confidenceScore(sufficiencyResult.getConfidenceScore())
                    .interpretationSummary(aiRequirements.getContextSummary())
                    .recommendations(List.of())
                    .build();
        }
        
        // Step 3: Get products with metadata
        List<Product> products = productRepository.findAllWithDetails(
            PageRequest.of(0, Math.min(100, request.getLimit() * 3)))
            .getContent();
        
        List<Long> productIds = products.stream()
            .map(Product::getId)
            .collect(Collectors.toList());
        
        List<ProductMetadata> allMetadata = metadataRepository.findByProductIdIn(productIds);
        Map<Long, ProductMetadata> metadataMap = allMetadata.stream()
            .collect(Collectors.toMap(ProductMetadata::getProductId, m -> m, (m1, m2) -> m1));
        
        // Step 4: Calculate AI-based match scores
        List<ProductRecommendationResponse> recommendations = products.stream()
            .map(product -> {
                try {
                    ProductMetadata metadata = metadataMap.get(product.getId());
                    
                    if (metadata == null || !Boolean.TRUE.equals(metadata.getIsAnalyzed())) {
                        return null;
                    }
                    
                    metadata.initializeNullCollections();
                    
                    // Calculate AI-powered match score
                    double score = calculateAIBasedMatchScore(metadata, aiRequirements);
                    if (score <= 0) {
                        return null;
                    }
                    
                    // Apply price filter if specified
                    if (request.getMaxPrice() != null && product.getDetails() != null) {
                        var firstDetail = product.getDetails().iterator().next();
                        if (firstDetail.getPrice().doubleValue() > request.getMaxPrice()) {
                            return null;
                        }
                    }
                    
                    List<String> matchingAttributes = getAIMatchingAttributes(metadata, aiRequirements);
                    return mapToResponse(product, score, matchingAttributes);
                } catch (Exception e) {
                    log.error("Error processing product {}: {}", product.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(ProductRecommendationResponse::getMatchScore).reversed())
            .limit(Math.max(1, Math.min(50, request.getLimit())))
            .collect(Collectors.toList());
        
        // Step 5: Determine response status based on results
        com.spring.fit.backend.recommendation.dto.response.NaturalLanguageRecommendationResponse.RecommendationStatus status;
        String message;
        List<String> suggestedQuestions = new ArrayList<>();
        
        if (recommendations.isEmpty()) {
            status = com.spring.fit.backend.recommendation.dto.response.NaturalLanguageRecommendationResponse.RecommendationStatus.NO_RESULTS;
            message = "I couldn't find products matching your requirements. Could you provide more details to help me find better options?";
            suggestedQuestions = generateSuggestedQuestions(aiRequirements);
        } else if (sufficiencyResult.getConfidenceScore() < 60.0) {
            status = com.spring.fit.backend.recommendation.dto.response.NaturalLanguageRecommendationResponse.RecommendationStatus.PARTIAL_RESULTS;
            message = "I found some options, but with more details I could provide better recommendations!";
            suggestedQuestions = generateSuggestedQuestions(aiRequirements);
        } else {
            status = com.spring.fit.backend.recommendation.dto.response.NaturalLanguageRecommendationResponse.RecommendationStatus.SUCCESS;
            message = "Here are my recommendations based on your preferences!";
        }
        
        return com.spring.fit.backend.recommendation.dto.response.NaturalLanguageRecommendationResponse.builder()
                .status(status)
                .recommendations(recommendations)
                .message(message)
                .suggestedQuestions(suggestedQuestions)
                .confidenceScore(sufficiencyResult.getConfidenceScore())
                .interpretationSummary(aiRequirements.getContextSummary())
                .build();
    }
    
    /**
     * Calculates match score based on AI-interpreted requirements with dynamic weighting.
     */
    private double calculateAIBasedMatchScore(ProductMetadata metadata, 
            com.spring.fit.backend.recommendation.dto.response.AIInterpretedRequirements aiRequirements) {
        
        if (metadata == null || aiRequirements == null) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        Map<String, Double> importanceScores = aiRequirements.getImportanceScores();
        
        // Category matching with AI weight (HIGH PRIORITY)
        if (aiRequirements.getCategories() != null && !aiRequirements.getCategories().isEmpty()
                && metadata.getProduct() != null && metadata.getProduct().getCategories() != null) {
            boolean matches = aiRequirements.getCategories().stream()
                    .anyMatch(reqCategory -> metadata.getProduct().getCategories().stream()
                            .anyMatch(prodCategory -> prodCategory != null && 
                                    (prodCategory.getSlug().equalsIgnoreCase(reqCategory) 
                                    || prodCategory.getName().toLowerCase().contains(reqCategory.toLowerCase()))));
            if (matches) {
                double weight = importanceScores.getOrDefault("category", 35.0);
                totalScore += weight;
            }
        }
        
        // Style matching with AI weight
        if (aiRequirements.getStyles() != null && !aiRequirements.getStyles().isEmpty()
                && metadata.getStyles() != null && !metadata.getStyles().isEmpty()) {
            boolean matches = aiRequirements.getStyles().stream()
                    .anyMatch(style -> metadata.getStyles().stream()
                            .anyMatch(s -> s != null && s.equalsIgnoreCase(style)));
            if (matches) {
                double weight = importanceScores.getOrDefault("style", 20.0);
                totalScore += weight;
            }
        }
        
        // Occasion matching with AI weight
        if (aiRequirements.getOccasions() != null && !aiRequirements.getOccasions().isEmpty()
                && metadata.getOccasions() != null) {
            boolean matches = aiRequirements.getOccasions().stream()
                    .anyMatch(occasion -> metadata.getOccasions().stream()
                            .anyMatch(o -> o != null && o.equalsIgnoreCase(occasion)));
            if (matches) {
                double weight = importanceScores.getOrDefault("occasion", 25.0);
                totalScore += weight;
            }
        }
        
        // Weather-based matching with AI weight
        if (aiRequirements.getWeatherPreferences() != null && !aiRequirements.getWeatherPreferences().isEmpty()
                && metadata.getSuitableForWeather() != null) {
            boolean matches = aiRequirements.getWeatherPreferences().stream()
                    .anyMatch(weather -> metadata.getSuitableForWeather().stream()
                            .anyMatch(w -> w != null && w.toLowerCase().contains(weather.toLowerCase())));
            if (matches) {
                double weight = importanceScores.getOrDefault("weather", 15.0);
                totalScore += weight;
            }
        }
        
        // Color matching with AI weight
        if (aiRequirements.getColors() != null && !aiRequirements.getColors().isEmpty()
                && metadata.getColors() != null && !metadata.getColors().isEmpty()) {
            boolean matches = aiRequirements.getColors().stream()
                    .anyMatch(color -> metadata.getColors().stream()
                            .anyMatch(c -> c != null && c.equalsIgnoreCase(color)));
            if (matches) {
                double weight = importanceScores.getOrDefault("color", 10.0);
                totalScore += weight;
            }
        }
        
        // Material matching
        if (aiRequirements.getMaterials() != null && !aiRequirements.getMaterials().isEmpty()
                && metadata.getMaterials() != null && !metadata.getMaterials().isEmpty()) {
            boolean matches = aiRequirements.getMaterials().stream()
                    .anyMatch(material -> metadata.getMaterials().stream()
                            .anyMatch(m -> m != null && m.equalsIgnoreCase(material)));
            if (matches) {
                totalScore += 5.0;
            }
        }
        
        // Boost score based on urgency level (multiply by urgency factor)
        if (aiRequirements.getUrgencyLevel() != null && aiRequirements.getUrgencyLevel() > 3) {
            totalScore *= 1.1; // 10% boost for high-urgency requests
        }
        
        // Base score for analyzed products
        if (Boolean.TRUE.equals(metadata.getIsAnalyzed())) {
            totalScore += 5.0;
        }
        
        return Math.min(100.0, totalScore);
    }
    
    /**
     * Gets matching attributes based on AI interpretation.
     */
    private List<String> getAIMatchingAttributes(ProductMetadata metadata,
            com.spring.fit.backend.recommendation.dto.response.AIInterpretedRequirements aiRequirements) {
        
        List<String> matchingAttributes = new ArrayList<>();
        
        if (metadata == null || aiRequirements == null) {
            return matchingAttributes;
        }
        
        // Add matching categories (HIGH PRIORITY)
        if (aiRequirements.getCategories() != null && metadata.getProduct() != null 
                && metadata.getProduct().getCategories() != null) {
            aiRequirements.getCategories().stream()
                    .filter(reqCategory -> metadata.getProduct().getCategories().stream()
                            .anyMatch(prodCategory -> prodCategory != null && 
                                    (prodCategory.getSlug().equalsIgnoreCase(reqCategory)
                                    || prodCategory.getName().toLowerCase().contains(reqCategory.toLowerCase()))))
                    .limit(2)
                    .forEach(category -> {
                        // Get the actual category name for display
                        metadata.getProduct().getCategories().stream()
                                .filter(pc -> pc.getSlug().equalsIgnoreCase(category) 
                                        || pc.getName().toLowerCase().contains(category.toLowerCase()))
                                .findFirst()
                                .ifPresent(pc -> matchingAttributes.add("Category: " + pc.getName()));
                    });
        }
        
        // Add matching styles
        if (aiRequirements.getStyles() != null && metadata.getStyles() != null) {
            aiRequirements.getStyles().stream()
                    .filter(style -> metadata.getStyles().stream()
                            .anyMatch(s -> s != null && s.equalsIgnoreCase(style)))
                    .forEach(matchingAttributes::add);
        }
        
        // Add matching occasions
        if (aiRequirements.getOccasions() != null && metadata.getOccasions() != null) {
            aiRequirements.getOccasions().stream()
                    .filter(occasion -> metadata.getOccasions().stream()
                            .anyMatch(o -> o != null && o.equalsIgnoreCase(occasion)))
                    .forEach(matchingAttributes::add);
        }
        
        // Add weather preferences
        if (aiRequirements.getWeatherContext() != null 
                && aiRequirements.getWeatherContext().getNeedsWeatherData()) {
            matchingAttributes.add("Weather-appropriate: " + 
                aiRequirements.getWeatherContext().getWeatherAdvice());
        }
        
        // Add colors
        if (aiRequirements.getColors() != null && metadata.getColors() != null) {
            aiRequirements.getColors().stream()
                    .filter(color -> metadata.getColors().stream()
                            .anyMatch(c -> c != null && c.equalsIgnoreCase(color)))
                    .limit(2) // Limit to avoid too many attributes
                    .forEach(matchingAttributes::add);
        }
        
        return matchingAttributes;
    }
    
    /**
     * Checks if the AI-interpreted requirements contain sufficient information
     * to make confident recommendations.
     */
    private InformationSufficiencyResult checkInformationSufficiency(
            com.spring.fit.backend.recommendation.dto.response.AIInterpretedRequirements aiRequirements) {
        
        int criteriaCount = 0;
        List<String> missingCriteria = new ArrayList<>();
        double confidenceScore = 0.0;
        
        // Check categories (HIGHEST PRIORITY)
        if (aiRequirements.getCategories() != null && !aiRequirements.getCategories().isEmpty()) {
            criteriaCount++;
            confidenceScore += 35.0;
        } else {
            missingCriteria.add("category");
        }
        
        // Check styles
        if (aiRequirements.getStyles() != null && !aiRequirements.getStyles().isEmpty()) {
            criteriaCount++;
            confidenceScore += 20.0;
        } else {
            missingCriteria.add("style");
        }
        
        // Check occasions
        if (aiRequirements.getOccasions() != null && !aiRequirements.getOccasions().isEmpty()) {
            criteriaCount++;
            confidenceScore += 25.0;
        } else {
            missingCriteria.add("occasion");
        }
        
        // Check weather/season preferences
        if ((aiRequirements.getWeatherPreferences() != null && !aiRequirements.getWeatherPreferences().isEmpty())
                || (aiRequirements.getWeatherContext() != null 
                    && Boolean.TRUE.equals(aiRequirements.getWeatherContext().getNeedsWeatherData()))) {
            criteriaCount++;
            confidenceScore += 15.0;
        } else {
            missingCriteria.add("weather");
        }
        
        // Check colors
        if (aiRequirements.getColors() != null && !aiRequirements.getColors().isEmpty()) {
            criteriaCount++;
            confidenceScore += 10.0;
        } else {
            missingCriteria.add("color");
        }
        
        // Check body types
        if (aiRequirements.getBodyTypes() != null && !aiRequirements.getBodyTypes().isEmpty()
                && !aiRequirements.getBodyTypes().contains("any")) {
            criteriaCount++;
            confidenceScore += 5.0;
        } else {
            missingCriteria.add("bodyType");
        }
        
        // Determine if information is sufficient
        // We need at least 2 criteria to make decent recommendations
        boolean isSufficient = criteriaCount >= 2 && confidenceScore >= 40.0;
        
        String message;
        if (criteriaCount == 0) {
            message = "I need more information to help you find the perfect outfit. Could you tell me more about what you're looking for?";
        } else if (criteriaCount == 1) {
            message = "I have some basic information, but I'd love to know more details to find the best matches for you!";
        } else if (confidenceScore < 60.0) {
            message = "I have a good understanding, but a few more details would help me narrow down the perfect options!";
        } else {
            message = "Great! I have enough information to find some excellent recommendations for you.";
        }
        
        List<String> suggestedQuestions = generateSuggestedQuestionsFromMissing(missingCriteria, aiRequirements);
        
        return InformationSufficiencyResult.builder()
                .isSufficient(isSufficient)
                .confidenceScore(confidenceScore)
                .criteriaCount(criteriaCount)
                .missingCriteria(missingCriteria)
                .message(message)
                .suggestedQuestions(suggestedQuestions)
                .build();
    }
    
    /**
     * Generates suggested questions based on missing criteria.
     */
    private List<String> generateSuggestedQuestions(
            com.spring.fit.backend.recommendation.dto.response.AIInterpretedRequirements aiRequirements) {
        
        List<String> questions = new ArrayList<>();
        
        // Check what's missing and suggest accordingly
        if (aiRequirements.getCategories() == null || aiRequirements.getCategories().isEmpty()) {
            questions.add("What type of item are you looking for? (e.g., t-shirt, pants, shoes, jacket, dress)");
        }
        
        if (aiRequirements.getStyles() == null || aiRequirements.getStyles().isEmpty()) {
            questions.add("What style are you looking for? (e.g., casual, formal, sporty, elegant, streetwear)");
        }
        
        if (aiRequirements.getOccasions() == null || aiRequirements.getOccasions().isEmpty()) {
            questions.add("What's the occasion? (e.g., work, party, date, gym, everyday, wedding)");
        }
        
        if (aiRequirements.getColors() == null || aiRequirements.getColors().isEmpty()) {
            questions.add("Do you have any color preferences? (e.g., black, blue, red, white, pastels)");
        }
        
        if ((aiRequirements.getWeatherPreferences() == null || aiRequirements.getWeatherPreferences().isEmpty())
                && (aiRequirements.getWeatherContext() == null 
                    || !Boolean.TRUE.equals(aiRequirements.getWeatherContext().getNeedsWeatherData()))) {
            questions.add("What's the weather like where you are? (e.g., hot summer, cold winter, rainy)");
        }
        
        if (aiRequirements.getBodyTypes() == null || aiRequirements.getBodyTypes().isEmpty()
                || aiRequirements.getBodyTypes().contains("any")) {
            questions.add("What's your body type? (e.g., athletic, curvy, slim, plus-size) - This helps us find the most flattering fit!");
        }
        
        if (aiRequirements.getMaterials() == null || aiRequirements.getMaterials().isEmpty()) {
            questions.add("Any material preferences? (e.g., cotton, denim, silk, breathable fabrics)");
        }
        
        // Limit to top 3-4 most important questions
        return questions.stream().limit(4).collect(Collectors.toList());
    }
    
    /**
     * Generates suggested questions based on specific missing criteria.
     */
    private List<String> generateSuggestedQuestionsFromMissing(
            List<String> missingCriteria,
            com.spring.fit.backend.recommendation.dto.response.AIInterpretedRequirements aiRequirements) {
        
        List<String> questions = new ArrayList<>();
        
        for (String criteria : missingCriteria) {
            switch (criteria.toLowerCase()) {
                case "category":
                    questions.add("What type of clothing are you looking for? (e.g., shirts, pants, shoes, jackets, dresses, bags)");
                    break;
                case "style":
                    questions.add("What style do you prefer? (e.g., casual, formal, streetwear, minimalist, sporty)");
                    break;
                case "occasion":
                    questions.add("Where will you wear this? (e.g., office, wedding, date night, gym, daily wear)");
                    break;
                case "color":
                    questions.add("What colors do you like? (e.g., neutral tones, bright colors, dark colors, pastels)");
                    break;
                case "weather":
                    questions.add("What's the weather/season? (e.g., summer heat, winter cold, rainy season)");
                    break;
                case "bodytype":
                    questions.add("What's your body type? This helps find the most flattering fit!");
                    break;
            }
        }
        
        // Add context-aware questions based on what we already know
        if (aiRequirements.getOccasions() != null && !aiRequirements.getOccasions().isEmpty()) {
            String occasion = aiRequirements.getOccasions().get(0);
            if (occasion.toLowerCase().contains("formal") || occasion.toLowerCase().contains("business")) {
                questions.add("Do you prefer a classic or modern take on formal wear?");
            }
        }
        
        // Add category-specific questions
        if (aiRequirements.getCategories() != null && !aiRequirements.getCategories().isEmpty()) {
            String category = aiRequirements.getCategories().get(0).toLowerCase();
            if (category.contains("giay") || category.contains("shoe") || category.contains("sneaker")) {
                questions.add("What size and fit do you prefer for shoes?");
            } else if (category.contains("ao") || category.contains("shirt") || category.contains("top")) {
                questions.add("Do you prefer a fitted or loose fit for tops?");
            }
        }
        
        // Limit to top 3-4 questions
        return questions.stream().limit(4).collect(Collectors.toList());
    }
    
    /**
     * Inner class to hold information sufficiency check results.
     */
    @lombok.Builder
    @lombok.Data
    private static class InformationSufficiencyResult {
        private boolean isSufficient;
        private double confidenceScore;
        private int criteriaCount;
        private List<String> missingCriteria;
        private String message;
        private List<String> suggestedQuestions;
    }
}