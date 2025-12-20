package com.spring.fit.backend.user.service;

import com.spring.fit.backend.security.service.EmailService;
import com.spring.fit.backend.user.domain.enums.RankThreshold;
import com.spring.fit.backend.user.event.UserRankUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRankNotificationService {

    private final EmailService emailService;

    @EventListener
    public void handleUserRankUpdated(UserRankUpdatedEvent event) {
        log.info("User {} rank updated from {} to {}", 
                event.getUserId(), event.getPreviousRankId(), event.getNewRankId());

        try {
            sendRankUpgradeNotification(event);
        } catch (Exception e) {
            log.error("Failed to send rank upgrade notification for user: {}", event.getUserId(), e);
        }
    }

    private void sendRankUpgradeNotification(UserRankUpdatedEvent event) {
        // Only send notification for rank upgrades (not downgrades)
        if (event.getNewRankId() > event.getPreviousRankId()) {
            RankThreshold newRank = RankThreshold.fromRankId(event.getNewRankId());
            
            // Here you can integrate with your email service
            // emailService.sendRankUpgradeEmail(event.getUserId(), newRank.name(), event.getTotalSpent());
            
            log.info("Rank upgrade notification sent to user: {} - New rank: {}", 
                    event.getUserId(), newRank.name());
        }
    }
}