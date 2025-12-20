package com.spring.fit.backend.report.scheduler;

import com.spring.fit.backend.report.service.DailyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task để tự động gửi daily report mỗi ngày
 * Chạy vào 6:00 AM mỗi ngày (theo timezone của server)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyReportScheduler {

    private final DailyReportService dailyReportService;

    /**
     * Email nhận daily report (có thể config trong application.properties)
     * Default: admin@fit.com
     */
    @Value("${app.report.daily-recipient-email:lhphuc12102003@gmail.com}")
    private String recipientEmail;

    /**
     * Gửi daily report tự động mỗi ngày vào 6:00 AM
     * Cron expression: "0 0 6 * * *"
     * - 0: giây (0)
     * - 0: phút (0)
     * - 6: giờ (6 AM)
     * - *: ngày trong tháng (mọi ngày)
     * - *: tháng (mọi tháng)
     * - *: thứ trong tuần (mọi thứ)
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void scheduleDailyReport() {
        log.info("Scheduled daily report started for email: {}", recipientEmail);
        try {
            dailyReportService.sendDailyReport(recipientEmail);
            log.info("Scheduled daily report completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled daily report: {}", e.getMessage(), e);
            // Không throw exception để tránh làm gián đoạn scheduler
        }
    }
}

