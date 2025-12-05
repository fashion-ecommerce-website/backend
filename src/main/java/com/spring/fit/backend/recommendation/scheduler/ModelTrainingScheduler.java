package com.spring.fit.backend.recommendation.scheduler;

import com.spring.fit.backend.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task để tự động train model recommendation mỗi tuần
 * Chạy vào 2:00 AM mỗi Chủ nhật (theo timezone của server)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelTrainingScheduler {

    private final RecommendationService recommendationService;

    /**
     * Train model tự động mỗi tuần vào 2:00 AM Chủ nhật
     * Cron expression: "0 0 2 ? * SUN"
     * - 0: giây (0)
     * - 0: phút (0)
     * - 2: giờ (2 AM)
     * - ?: ngày trong tháng (không quan tâm)
     * - *: tháng (mọi tháng)
     * - SUN: thứ trong tuần (Chủ nhật)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduleWeeklyModelTraining() {
        log.info("Scheduled weekly model training started");
        try {
            recommendationService.trainModel();
            log.info("Scheduled weekly model training completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled model training: {}", e.getMessage(), e);
            // Không throw exception để tránh làm gián đoạn scheduler
        }
    }
}

