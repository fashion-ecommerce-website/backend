package com.spring.fit.backend.user.domain.enums;

import java.math.BigDecimal;

public enum RankThreshold {
    BRONZE(1, BigDecimal.ZERO),
    SILVER(2, new BigDecimal("1000000")),
    GOLD(3, new BigDecimal("5000000")),
    PLATINUM(4, new BigDecimal("15000000")),
    DIAMOND(5, new BigDecimal("50000000"));

    private final Short rankId;
    private final BigDecimal threshold;

    RankThreshold(int rankId, BigDecimal threshold) {
        this.rankId = (short) rankId;
        this.threshold = threshold;
    }

    public Short getRankId() {
        return rankId;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public static Short calculateRankId(BigDecimal totalSpent) {
        RankThreshold[] ranks = values();
        
        // Start from highest rank and work down
        for (int i = ranks.length - 1; i >= 0; i--) {
            if (totalSpent.compareTo(ranks[i].getThreshold()) >= 0) {
                return ranks[i].getRankId();
            }
        }
        
        return BRONZE.getRankId(); // Default to Bronze
    }

    public static RankThreshold fromRankId(Short rankId) {
        for (RankThreshold rank : values()) {
            if (rank.getRankId().equals(rankId)) {
                return rank;
            }
        }
        return BRONZE;
    }
}