package com.spring.fit.backend.recommendation.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Statistics for a specific size, weighted by user similarity.
 * 
 * The weightedCount formula:
 * weightedCount = Σ(orderWeight) where orderWeight = similarity × qualityWeight × successWeight × recencyWeight
 */
@Data
public class SizeStatistics {
    // Raw counts (unweighted)
    private int successfulOrders = 0;    // DELIVERED + in-transit
    private int returnedOrders = 0;      // RETURNED + REFUNDED
    private int cancelledOrders = 0;     // CANCELLED
    
    // Weighted counts (by user similarity)
    private double weightedSuccessful = 0.0;
    private double weightedReturned = 0.0;
    private double weightedCancelled = 0.0;
    
    // Rating tracking
    private double totalWeightedRating = 0.0;
    private double totalSimilarityForRating = 0.0;
    
    // Recency tracking
    private List<WeightedPurchase> purchases = new ArrayList<>();
    
    // Calculated fields
    private double successRate;
    private double returnRate;
    private double cancelRate;
    private double avgRating;
    private double weightedCount;  // Final score for this size
    
    /**
     * Add a successful order (DELIVERED or in-transit) with user similarity weight.
     * 
     * @param similarity User similarity score (0.0 to 1.0)
     * @param rating Review rating (0 if no review)
     * @param purchaseDate Date of purchase
     */
    public void addSuccessfulOrder(double similarity, double rating, LocalDateTime purchaseDate) {
        this.successfulOrders++;
        this.weightedSuccessful += similarity;
        
        if (rating > 0) {
            this.totalWeightedRating += rating * similarity;
            this.totalSimilarityForRating += similarity;
        }
        
        if (purchaseDate != null) {
            this.purchases.add(new WeightedPurchase(similarity, purchaseDate, true, false));
        }
    }
    
    /**
     * Add a returned order (RETURNED or REFUNDED) with user similarity weight.
     * Returned orders indicate potential size mismatch.
     */
    public void addReturnedOrder(double similarity, double rating, LocalDateTime purchaseDate) {
        this.returnedOrders++;
        this.weightedReturned += similarity;
        
        // Don't count rating for returned orders (likely negative experience)
        
        if (purchaseDate != null) {
            this.purchases.add(new WeightedPurchase(similarity, purchaseDate, false, true));
        }
    }
    
    /**
     * Add a cancelled order with user similarity weight.
     * Cancelled orders are neutral (not related to size fit).
     */
    public void addCancelledOrder(double similarity, LocalDateTime purchaseDate) {
        this.cancelledOrders++;
        this.weightedCancelled += similarity;
        
        if (purchaseDate != null) {
            this.purchases.add(new WeightedPurchase(similarity, purchaseDate, false, false));
        }
    }
    
    /**
     * Calculate final weighted count and rates.
     * 
     * Formula:
     * weightedCount = Σ(similarity × recencyWeight × successWeight)
     * 
     * Where:
     * - similarity: How similar the user is to current user (0.0 to 1.0)
     * - recencyWeight: How recent the purchase was (0.3 to 1.0)
     * - successWeight: 1.0 for successful, -0.5 for returned, 0 for cancelled
     */
    public void calculateRates() {
        int totalOrders = getTotalOrders();
        
        if (totalOrders > 0) {
            this.successRate = (double) successfulOrders / totalOrders;
            this.returnRate = (double) returnedOrders / totalOrders;
            this.cancelRate = (double) cancelledOrders / totalOrders;
        }
        
        // Calculate weighted average rating
        if (totalSimilarityForRating > 0) {
            this.avgRating = totalWeightedRating / totalSimilarityForRating;
        }
        
        // Calculate final weighted count
        // Each purchase contributes: similarity × recencyWeight × successFactor
        double finalWeightedCount = 0.0;
        
        for (WeightedPurchase purchase : purchases) {
            double recencyWeight = calculateRecencyWeight(purchase.purchaseDate);
            double successFactor;
            
            if (purchase.isSuccessful) {
                // Successful order: positive contribution
                successFactor = 1.0;
            } else if (purchase.isReturned) {
                // Returned order: negative contribution (size didn't fit)
                successFactor = -0.5;
            } else {
                // Cancelled: no contribution
                successFactor = 0.0;
            }
            
            finalWeightedCount += purchase.similarity * recencyWeight * successFactor;
        }
        
        // Apply quality bonus based on average rating
        double qualityMultiplier = calculateQualityMultiplier(this.avgRating);
        this.weightedCount = Math.max(0, finalWeightedCount * qualityMultiplier);
    }
    
    private double calculateRecencyWeight(LocalDateTime purchaseDate) {
        if (purchaseDate == null) return 0.5;
        
        long daysAgo = ChronoUnit.DAYS.between(purchaseDate, LocalDateTime.now());
        if (daysAgo <= 90) return 1.0;      // 0-3 months: 100%
        if (daysAgo <= 180) return 0.85;    // 3-6 months: 85%
        if (daysAgo <= 365) return 0.70;    // 6-12 months: 70%
        if (daysAgo <= 730) return 0.50;    // 1-2 years: 50%
        return 0.30;                         // >2 years: 30%
    }
    
    private double calculateQualityMultiplier(double avgRating) {
        if (avgRating >= 4.5) return 1.2;   // Excellent: +20%
        if (avgRating >= 4.0) return 1.1;   // Good: +10%
        if (avgRating >= 3.5) return 1.0;   // Average: no change
        if (avgRating > 0) return 0.9;      // Below average: -10%
        return 1.0;                          // No rating: no change
    }
    
    public int getTotalOrders() {
        return successfulOrders + returnedOrders + cancelledOrders;
    }
    
    public int getRatingCount() {
        return totalSimilarityForRating > 0 ? (int) Math.ceil(totalSimilarityForRating) : 0;
    }
    
    /**
     * Inner class to track individual purchases with their weights
     */
    private static class WeightedPurchase {
        final double similarity;
        final LocalDateTime purchaseDate;
        final boolean isSuccessful;
        final boolean isReturned;
        
        WeightedPurchase(double similarity, LocalDateTime purchaseDate, boolean isSuccessful, boolean isReturned) {
            this.similarity = similarity;
            this.purchaseDate = purchaseDate;
            this.isSuccessful = isSuccessful;
            this.isReturned = isReturned;
        }
    }
}
