package com.spring.fit.backend.user.service;

import java.util.List;

public interface RecentViewService {
    void addViewed(long userId, long productId);
    List<Long> getRecentIds(long userId);
    void removeSelected(long userId, List<Long> productIds);
    void clearAll(long userId);
}

