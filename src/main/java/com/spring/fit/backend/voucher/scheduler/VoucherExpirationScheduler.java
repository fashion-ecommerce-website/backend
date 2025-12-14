package com.spring.fit.backend.voucher.scheduler;

import com.spring.fit.backend.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task để tự động set inactive cho các voucher đã hết hạn
 * Chạy mỗi đêm lúc 00:05 AM
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VoucherExpirationScheduler {

    private final VoucherRepository voucherRepository;

    /**
     * Tự động deactivate các voucher đã hết hạn
     * Chạy mỗi ngày lúc 00:00 AM
     * 
     * Cron expression: "0 0 0 * * *"
     * - 0: giây (0)
     * - 5: phút (5)
     * - 0: giờ (0 = 12:05 AM)
     * - *: ngày trong tháng (mọi ngày)
     * - *: tháng (mọi tháng)
     * - *: thứ trong tuần (mọi thứ)
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deactivateExpiredVouchers() {
        log.info("=== Starting scheduled task: Deactivate expired vouchers ===");
        LocalDateTime now = LocalDateTime.now();
        
        try {
            long expiredCount = voucherRepository.countExpiredActiveVouchers(now);
            
            if (expiredCount == 0) {
                log.info("No expired vouchers found to deactivate");
                return;
            }
            
            log.info("Found {} expired voucher(s) to deactivate", expiredCount);
            
            int updatedCount = voucherRepository.deactivateExpiredVouchers(now);
            
            log.info("Successfully deactivated {} expired voucher(s)", updatedCount);
            
        } catch (Exception e) {
            log.error("Error during deactivate expired vouchers: {}", e.getMessage(), e);
        }
        
        log.info("=== Completed scheduled task: Deactivate expired vouchers ===");
    }
}
