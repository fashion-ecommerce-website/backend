package com.spring.fit.backend.recommendation.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Statistics for a specific size including success/return/cancel rates
 */
@Data
public class SizeStatistics {
    // Order counts by status
    private int successfulOrders = 0;    // DELIVERED + in-transit
    private int returnedOrders = 0;      // RETURNED + REFUNDED
    private int cancelledOrders = 0;     // CANCELLED
    
    // Rating data
    private double totalRating = 0.0;
    private int ratingCount = 0;
    
    // Purchase dates for recency calculation
    private List<LocalDateTime> purchaseDates = new ArrayList<>();
    
    // Calculated fields
    private double successRate;
    private double returnRate;
    private double cancelRate;
    private double avgRating;
    private double avgRecencyWeight;
    private double weightedCount;  // Final score for recommendation
    
    /**
     * Add a successful order (delivered and kept)
     */
    public void addSuccessfulOrder(int count, double rating, LocalDateTime date) {
        this.successfulOrders += count;
        if (rating > 0) {
            this.totalRating += rating * count;
            this.ratingCount += count;
        }
        this.purchaseDates.add(date);
    }
    
    /**
     * Add a returned order (returned or refunded)
     */
    public void addReturnedOrder(int count, double rating, LocalDateTime date) {
        this.returnedOrders += count;
        // Still track rating for returned orders (might indicate quality issues)
        if (rating > 0) {
            this.totalRating += rating * count;
            this.ratingCount += count;
        }
        this.purchaseDates.add(date);
    }
    
    /**
     * Add a cancelled order (cancelled before delivery)
     */
    public void addCancelledOrder(int count, double rating, LocalDateTime date) {
        this.cancelledOrders += count;
        this.purchaseDates.add(date);
    }
    
    /**
     * Calculate all rates and weighted count
     */
    public void calculateRates() {
        int totalOrders = successfulOrders + returnedOrders + cancelledOrders;
        
        if (totalOrders > 0) {
            this.successRate = (double) successfulOrders / totalOrders;
            this.returnRate = (double) returnedOrders / totalOrders;
            this.cancelRate = (double) cancelledOrders / totalOrders;
        }
        
        if (ratingCount > 0) {
            this.avgRating = totalRating / ratingCount;
        }
        
        // Calculate average recency weight
        if (!purchaseDates.isEmpty()) {
            this.avgRecencyWeight = purchaseDates.stream()
                .mapToDouble(this::calculateRecencyWeight)
                .average()
                .orElse(0.5);
        } else {
            this.avgRecencyWeight = 0.5;
        }
        
        // Calculate weighted count (final score)
        // Combines: quality (from reviews) + success (from returns) + recency (time decay)
        double qualityWeight = calculateQualityWeight(this.avgRating);
        double successWeight = 1.0 - (returnRate * 0.7) - (cancelRate * 0.3);
        
        this.weightedCount = totalOrders * qualityWeight * successWeight * avgRecencyWeight;
    }
    
    /**
     * Calculate recency weight based on purchase date
     * Recent purchases have higher weight
     */
    private double calculateRecencyWeight(LocalDateTime purchaseDate) {
        long daysAgo = ChronoUnit.DAYS.between(purchaseDate, LocalDateTime.now());
        
        if (daysAgo <= 90) return 1.0;      // 3 months: 100%
        if (daysAgo <= 180) return 0.85;    // 6 months: 85%
        if (daysAgo <= 365) return 0.70;    // 1 year: 70%
        if (daysAgo <= 730) return 0.50;    // 2 years: 50%
        return 0.30;                         // Older: 30%
    }
    
    /**
     * Calculate quality weight based on review rating
     * KEEP EXISTING LOGIC - proven to work well
     */
    private double calculateQualityWeight(double avgRating) {
        if (avgRating >= 4.0) {
            return 1.0; // High ratings - full confidence
        } else if (avgRating > 0 && avgRating < 3.0) {
            return 0.5; // Low ratings - reduced confidence (possible wrong size)
        } else {
            return 0.8; // No review - slightly lower confidence
        }
    }
    
    /**
     * Get total number of orders
     */
    public int getTotalOrders() {
        return successfulOrders + returnedOrders + cancelledOrders;
    }
    
    /**
     * Get frequency (for compatibility with existing code)
     */
    public int getFrequency() {
        return getTotalOrders();
    }
}
