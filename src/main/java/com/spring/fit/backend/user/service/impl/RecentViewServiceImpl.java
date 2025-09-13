package com.spring.fit.backend.user.service.impl;

import com.spring.fit.backend.user.service.RecentViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecentViewServiceImpl implements RecentViewService {
    private final StringRedisTemplate redis;

    private static final int LIMIT = 12;
    private static final Duration TTL = Duration.ofDays(30);

    private String key(long userId) { return "recent:prod:" + userId; }

    @Override
    public void addViewed(long userId, long productId) {
        String k = key(userId);
        double now = System.currentTimeMillis();

        // ZADD (member trùng sẽ được update score)
        redis.opsForZSet().add(k, String.valueOf(productId), now);

        // Giữ tối đa 12 phần tử: xoá các phần tử cũ hơn (rank thấp nhất)
        Long size = redis.opsForZSet().zCard(k);
        if (size != null && size > LIMIT) {
            // remove oldest: rank 0 .. (size - LIMIT - 1)
            redis.opsForZSet().removeRange(k, 0, size - LIMIT - 1);
        }

        // TTL
        redis.expire(k, TTL);
    }

    @Override
    public List<Long> getRecentIds(long userId) {
        String k = key(userId);
        Set<String> raw = redis.opsForZSet().reverseRange(k, 0, LIMIT - 1);
        if (raw == null || raw.isEmpty()) return List.of();
        return raw.stream().map(Long::parseLong).collect(Collectors.toList());
    }

    @Override
    public void removeSelected(long userId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return;

        String k = key(userId);
        // Chuyển sang String[] vì StringRedisTemplate dùng chuỗi
        String[] members = productIds.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toArray(String[]::new);

        if (members.length == 0) return ;

        redis.opsForZSet().remove(k, (Object[]) members);
    }

    @Override
    public void clearAll(long userId) {
        redis.delete(key(userId));
    }
}

