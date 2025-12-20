package com.spring.fit.backend.user.service;

import com.spring.fit.backend.user.domain.enums.RankThreshold;
import com.spring.fit.backend.user.event.PaymentSuccessEvent;
import com.spring.fit.backend.user.event.UserRankUpdatedEvent;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRankService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Processing payment success for user: {}, amount: {}", 
                event.getUserId(), event.getAmount());

        try {
            updateUserSpendingAndRank(event.getUserId(), event.getOrderId(), event.getAmount());
        } catch (Exception e) {
            log.error("Failed to update user rank for user: {}", event.getUserId(), e);
            throw e;
        }
    }

    @Transactional
    public void updateUserSpendingAndRank(Long userId, Long orderId, BigDecimal amount) {
        // Get current user
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Calculate new total
        BigDecimal previousTotal = user.getTotalSpent();
        BigDecimal newTotal = previousTotal.add(amount);
        
        // Calculate ranks
        Short previousRankId = user.getRankId();
        Short newRankId = RankThreshold.calculateRankId(newTotal);

        // Update user spending
        user.setTotalSpent(newTotal);

        // Update user rank if changed
        if (!newRankId.equals(previousRankId)) {
            user.setRankId(newRankId);
            log.info("User {} rank updated from {} to {}", userId, previousRankId, newRankId);

            // Publish rank updated event
            eventPublisher.publishEvent(new UserRankUpdatedEvent(
                    userId, previousRankId, newRankId, newTotal));
        }

        userRepository.save(user);
    }

    public UserEntity getUserSpendingData(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    public Short calculateUserRank(Long userId) {
        UserEntity user = getUserSpendingData(userId);
        return RankThreshold.calculateRankId(user.getTotalSpent());
    }
}