package com.spring.fit.backend.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.keep-alive.enabled", havingValue = "true", matchIfMissing = true)
public class KeepAliveService {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.keep-alive.interval:300000}")
    private long keepAliveInterval;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final int MAX_REQUESTS_PER_HOUR = 60; 

    @Scheduled(fixedRate = 3600000)
    public void resetRequestCount() {
        requestCount.set(0);
        log.debug("🔄 Request counter reset at {}", LocalDateTime.now());
    }

    @Scheduled(fixedRateString = "${app.keep-alive.interval:300000}")
    public void keepAlive() {
        // Kiểm tra giới hạn requests
        if (requestCount.get() >= MAX_REQUESTS_PER_HOUR) {
            log.warn("⚠️ Max requests per hour reached, skipping keep alive");
            return;
        }

        try {
            String healthUrl = "http://localhost:" + serverPort + "/api/health/ping";
            
            restTemplate.getForObject(healthUrl, String.class);
            requestCount.incrementAndGet();
            
            log.debug("🔄 Keep alive successful at {} (interval: {}ms, requests: {})", 
                     LocalDateTime.now(), keepAliveInterval, requestCount.get());
            
        } catch (Exception e) {
            log.warn("⚠️ Keep alive failed: {}", e.getMessage());
        }
    }

    /**
     * Keep alive với endpoint khác để đa dạng hóa
     * Chạy ít thường xuyên hơn để tránh spam
     */
    @Scheduled(fixedRate = 600000) // 10 phút
    public void keepAliveHealth() {
        try {
            String healthUrl = "http://localhost:" + serverPort + "/api/health";
            
            restTemplate.getForObject(healthUrl, String.class);
            
            log.debug("🔄 Health keep alive successful at {}", LocalDateTime.now());
            
        } catch (Exception e) {
            log.warn("⚠️ Health keep alive failed: {}", e.getMessage());
        }
    }
}
