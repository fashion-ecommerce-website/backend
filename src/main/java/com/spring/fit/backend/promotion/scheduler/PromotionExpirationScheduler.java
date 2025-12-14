package com.spring.fit.backend.promotion.scheduler;

import com.spring.fit.backend.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task để tự động set inactive cho các promotion đã hết hạn
 * Chạy mỗi đêm lúc 00:10 AM
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionExpirationScheduler {

    private final PromotionRepository promotionRepository;

    /**
     * Tự động deactivate các promotion đã hết hạn
     * Chạy mỗi ngày lúc 00:10 AM (10 phút sau voucher scheduler để tránh conflict)
     * 
     * Cron expression: "0 10 0 * * *"
     * - 0: giây (0)
     * - 10: phút (10)
     * - 0: giờ (0 = 12:10 AM)
     * - *: ngày trong tháng (mọi ngày)
     * - *: tháng (mọi tháng)
     * - *: thứ trong tuần (mọi thứ)
     */
    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void deactivateExpiredPromotions() {
        log.info("=== Starting scheduled task: Deactivate expired promotions ===");
        LocalDateTime now = LocalDateTime.now();
        
        try {
            long expiredCount = promotionRepository.countExpiredActivePromotions(now);
            
            if (expiredCount == 0) {
                log.info("No expired promotions found to deactivate");
                return;
            }
            
            log.info("Found {} expired promotion(s) to deactivate", expiredCount);
            
            int updatedCount = promotionRepository.deactivateExpiredPromotions(now);
            
            log.info("Successfully deactivated {} expired promotion(s)", updatedCount);
            
        } catch (Exception e) {
            log.error("Error during deactivate expired promotions: {}", e.getMessage(), e);
        }
        
        log.info("=== Completed scheduled task: Deactivate expired promotions ===");
    }
}
