package com.spring.fit.backend.recommendation.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.repository.OrderRepository;
import com.spring.fit.backend.recommendation.domain.dto.SizeRecommendationResponse;
import com.spring.fit.backend.recommendation.domain.model.SimilarUser;
import com.spring.fit.backend.recommendation.domain.model.SizeStatistics;
import com.spring.fit.backend.recommendation.domain.model.UserCluster;
import com.spring.fit.backend.recommendation.service.SizeRecommendationService;
import com.spring.fit.backend.recommendation.util.SimilarityCalculator;
import com.spring.fit.backend.user.domain.entity.UserMeasurements;
import com.spring.fit.backend.user.repository.UserMeasurementsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Size Recommendation Service Implementation
 * 
 * This service uses Collaborative Filtering to recommend sizes based on:
 * 1. Finding similar users (same cluster: gender, BMI range, age group)
 * 2. Calculating Euclidean distance for body measurements
 * 3. Analyzing order history of similar users
 * 4. Calculating weighted recommendation based on success rate, ratings, and recency
 * 
 * NOTE: Rule-based fallback is handled by Frontend (has more detailed size charts per category)
 * Backend only returns null recommendation when no collaborative data is available.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SizeRecommendationServiceImpl implements SizeRecommendationService {

    private final UserMeasurementsRepository measurementsRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public SizeRecommendationResponse getSizeRecommendation(Long userId, Long productId, Integer similarUserLimit) {
        log.info("Getting size recommendation for user: {}, product: {}, limit: {}", userId, productId, similarUserLimit);
        
        // Get user measurements
        UserMeasurements userMeasurements = measurementsRepository.findByUserId(userId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User measurements not found"));
        
        // STEP 1: Clustering - Find users in same cluster
        UserCluster cluster = UserCluster.fromMeasurements(userMeasurements);
        List<UserMeasurements> clusterCandidates = measurementsRepository.findUsersInCluster(
            cluster.getGender(),
            cluster.getBmiMin(),
            cluster.getBmiMax(),
            cluster.getAgeMin(),
            cluster.getAgeMax(),
            userId
        );
        
        log.info("Found {} users in cluster (gender={}, bmi={}-{}, age={}-{})", 
            clusterCandidates.size(), cluster.getGender(), 
            cluster.getBmiMin(), cluster.getBmiMax(),
            cluster.getAgeMin(), cluster.getAgeMax());
        
        // STEP 2: Calculate similarity (body measurements + fit preference + body shapes)
        List<SimilarUser> similarUsers = clusterCandidates.stream()
            .map(candidate -> {
                // Calculate comprehensive similarity including categorical attributes
                double similarity = SimilarityCalculator.calculateSimilarity(userMeasurements, candidate);
                double distance = SimilarityCalculator.calculateEuclideanDistance(userMeasurements, candidate);
                return new SimilarUser(candidate, similarity, distance);
            })
            .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
            .limit(similarUserLimit != null ? similarUserLimit : 30)
            .collect(Collectors.toList());
        
        log.info("Found {} similar users after distance calculation", similarUsers.size());
        
        // No similar users found - return no data response (FE will handle rule-based)
        if (similarUsers.isEmpty()) {
            log.info("No similar users found, returning no data response for FE to handle rule-based");
            return buildNoDataResponse();
        }
        
        // STEP 3: Analyze order history with shipment status
        Map<String, SizeStatistics> sizeStats = analyzeSizeStatistics(similarUsers, productId);
        
        // No order history found - return no data response (FE will handle rule-based)
        if (sizeStats.isEmpty()) {
            log.info("No purchase history found for product {}, returning no data response for FE to handle rule-based", productId);
            return buildNoDataResponse();
        }
        
        // STEP 4: Calculate weighted recommendation
        return calculateWeightedRecommendation(similarUsers, sizeStats);
    }
    
    /**
     * STEP 3: Analyze size statistics from order history, weighted by user similarity.
     * 
     * Each order is weighted by the similarity score of the user who placed it.
     * Users more similar to the current user have higher influence on the recommendation.
     */
    private Map<String, SizeStatistics> analyzeSizeStatistics(
            List<SimilarUser> similarUsers,
            Long productId) {
        
        // Create userId -> similarity map for quick lookup
        Map<Long, Double> userSimilarityMap = similarUsers.stream()
            .collect(Collectors.toMap(
                SimilarUser::getUserId,
                SimilarUser::getSimilarity,
                (existing, replacement) -> existing  // Keep first if duplicate
            ));
        
        List<Long> userIds = new ArrayList<>(userSimilarityMap.keySet());
        
        // Query returns: userId, sizeLabel, shipmentStatus, orderStatus, rating, purchaseDate
        List<Object[]> orderHistory = orderRepository.findOrderHistoryWithShipmentStatus(userIds, productId);
        
        Map<String, SizeStatistics> sizeStats = new HashMap<>();
        
        for (Object[] row : orderHistory) {
            Long orderUserId = ((Number) row[0]).longValue();
            String size = (String) row[1];
            String shipmentStatus = row[2] != null ? row[2].toString() : "PENDING";
            String orderStatusStr = row[3] != null ? row[3].toString() : "UNFULFILLED";
            Double rating = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            LocalDateTime purchaseDate = (LocalDateTime) row[5];
            
            // Get similarity score for this user (default to 0.5 if not found)
            double similarity = userSimilarityMap.getOrDefault(orderUserId, 0.5);
            
            SizeStatistics stats = sizeStats.computeIfAbsent(size, k -> new SizeStatistics());
            
            // Classify based on shipment status first, then order status
            // Each order is weighted by user similarity
            if ("DELIVERED".equals(shipmentStatus)) {
                // Successfully delivered and kept - positive signal
                stats.addSuccessfulOrder(similarity, rating, purchaseDate);
            } else if ("RETURNED".equals(shipmentStatus) || "REFUNDED".equals(shipmentStatus)) {
                // Returned - likely size issue - negative signal
                stats.addReturnedOrder(similarity, rating, purchaseDate);
            } else if ("CANCELLED".equals(orderStatusStr)) {
                // Cancelled before delivery - neutral signal
                stats.addCancelledOrder(similarity, purchaseDate);
            } else {
                // In transit or pending - treat as successful for now
                stats.addSuccessfulOrder(similarity, rating, purchaseDate);
            }
        }
        
        // Calculate final weighted counts for all sizes
        sizeStats.values().forEach(SizeStatistics::calculateRates);
        
        log.info("Analyzed {} different sizes from order history (similarity-weighted)", sizeStats.size());
        sizeStats.forEach((size, stats) -> {
            log.debug("Size {}: total={}, success={}, returned={}, cancelled={}, weightedCount={:.3f}", 
                size, stats.getTotalOrders(), stats.getSuccessfulOrders(), 
                stats.getReturnedOrders(), stats.getCancelledOrders(), stats.getWeightedCount());
        });
        
        return sizeStats;
    }
    
    /**
     * STEP 4: Calculate weighted recommendation
     */
    private SizeRecommendationResponse calculateWeightedRecommendation(
            List<SimilarUser> similarUsers,
            Map<String, SizeStatistics> sizeStats) {
        
        // Find recommended size (using weightedCount from SizeStatistics)
        String recommendedSize = sizeStats.entrySet().stream()
            .max(Comparator.comparingDouble(e -> e.getValue().getWeightedCount()))
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (recommendedSize == null) {
            return buildNoDataResponse();
        }
        
        // Calculate confidence
        double totalWeightedCount = sizeStats.values().stream()
            .mapToDouble(SizeStatistics::getWeightedCount)
            .sum();
        
        double rawConfidence = totalWeightedCount > 0 
            ? sizeStats.get(recommendedSize).getWeightedCount() / totalWeightedCount 
            : 0.0;
        
        // Apply data quantity penalty - confidence should be lower when we have less data
        int totalOrders = sizeStats.values().stream()
            .mapToInt(SizeStatistics::getTotalOrders)
            .sum();
        
        double dataQuantityMultiplier = calculateDataQuantityMultiplier(totalOrders);
        double confidence = rawConfidence * dataQuantityMultiplier;
        
        // Build alternatives
        List<SizeRecommendationResponse.AlternativeSize> alternatives = sizeStats.entrySet().stream()
            .filter(e -> !e.getKey().equals(recommendedSize))
            .map(e -> SizeRecommendationResponse.AlternativeSize.builder()
                .size(e.getKey())
                .confidence(totalWeightedCount > 0 
                    ? e.getValue().getWeightedCount() / totalWeightedCount 
                    : 0.0)
                .build())
            .sorted((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()))
            .limit(3)
            .collect(Collectors.toList());
        
        // Build metadata
        double avgRating = sizeStats.values().stream()
            .filter(s -> s.getRatingCount() > 0)
            .mapToDouble(SizeStatistics::getAvgRating)
            .average()
            .orElse(0.0);
        
        SizeRecommendationResponse.RecommendationMetadata metadata = buildMetadata(
            similarUsers.size(),
            totalOrders,
            confidence,
            avgRating,
            !alternatives.isEmpty()
        );
        
        return SizeRecommendationResponse.builder()
            .recommendedSize(recommendedSize)
            .confidence(confidence)
            .alternatives(alternatives)
            .metadata(metadata)
            .hasMeasurements(true)
            .build();
    }
    
    /**
     * Build metadata for recommendation
     */
    private SizeRecommendationResponse.RecommendationMetadata buildMetadata(
            int totalSimilarUsers,
            int totalPurchases,
            double confidence,
            double avgRating,
            boolean hasAlternative) {
        
        // Determine confidence level
        SizeRecommendationResponse.ConfidenceLevel confidenceLevel;
        if (confidence >= 0.70) {
            confidenceLevel = SizeRecommendationResponse.ConfidenceLevel.HIGH;
        } else if (confidence >= 0.50) {
            confidenceLevel = SizeRecommendationResponse.ConfidenceLevel.MEDIUM;
        } else {
            confidenceLevel = SizeRecommendationResponse.ConfidenceLevel.LOW;
        }
        
        // Determine data quality
        SizeRecommendationResponse.DataQuality dataQuality;
        if (totalSimilarUsers >= 20 && avgRating >= 4.0) {
            dataQuality = SizeRecommendationResponse.DataQuality.EXCELLENT;
        } else if (totalSimilarUsers >= 10 && avgRating >= 3.5) {
            dataQuality = SizeRecommendationResponse.DataQuality.GOOD;
        } else if (totalSimilarUsers >= 5) {
            dataQuality = SizeRecommendationResponse.DataQuality.FAIR;
        } else {
            dataQuality = SizeRecommendationResponse.DataQuality.LIMITED;
        }
        
        double highRatingRatio = avgRating >= 4.0 ? 0.8 : (avgRating >= 3.5 ? 0.6 : 0.4);
        
        return SizeRecommendationResponse.RecommendationMetadata.builder()
            .totalSimilarUsers(totalSimilarUsers)
            .totalPurchases(totalPurchases)
            .averageRating(avgRating)
            .highRatingRatio(highRatingRatio)
            .confidenceLevel(confidenceLevel)
            .dataQuality(dataQuality)
            .hasCloseAlternative(hasAlternative)
            .build();
    }
    
    /**
     * Calculate data quantity multiplier to penalize confidence when we have too few orders.
     * This prevents 100% confidence when only 1-2 orders exist.
     * 
     * @param totalOrders Total number of orders for this product from similar users
     * @return Multiplier between 0.3 and 1.0
     */
    private double calculateDataQuantityMultiplier(int totalOrders) {
        if (totalOrders >= 10) return 1.0;      // 10+ orders: full confidence
        if (totalOrders >= 7) return 0.9;       // 7-9 orders: 90%
        if (totalOrders >= 5) return 0.8;       // 5-6 orders: 80%
        if (totalOrders >= 3) return 0.6;       // 3-4 orders: 60%
        if (totalOrders >= 2) return 0.5;       // 2 orders: 50%
        return 0.3;                              // 1 order: 30%
    }
    
    /**
     * Build response when no collaborative data is available.
     * Frontend will handle rule-based recommendation using its detailed size charts.
     */
    private SizeRecommendationResponse buildNoDataResponse() {
        return SizeRecommendationResponse.builder()
            .recommendedSize(null)
            .confidence(null)
            .alternatives(Collections.emptyList())
            .metadata(SizeRecommendationResponse.RecommendationMetadata.builder()
                .totalSimilarUsers(0)
                .totalPurchases(0)
                .averageRating(0.0)
                .highRatingRatio(0.0)
                .confidenceLevel(null)
                .dataQuality(SizeRecommendationResponse.DataQuality.LIMITED)
                .hasCloseAlternative(false)
                .build())
            .hasMeasurements(true)
            .build();
    }
}
