package com.spring.fit.backend.user.service;

import com.spring.fit.backend.user.domain.dto.response.UserRankResponse;
import com.spring.fit.backend.user.domain.entity.UserRank;
import com.spring.fit.backend.user.domain.enums.RankThreshold;
import com.spring.fit.backend.user.event.PaymentSuccessEvent;
import com.spring.fit.backend.user.event.UserRankUpdatedEvent;
import com.spring.fit.backend.user.event.UserRankChangedEvent;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.user.repository.UserRankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRankService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRankRepository userRankRepository;

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
            
            // Publish rank changed event for JWT refresh (if needed)
            eventPublisher.publishEvent(new UserRankChangedEvent(
                    userId, previousRankId, newRankId, user.getEmail()));
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

    public List<UserRankResponse> getAllUserRanks() {
        log.info("Inside UserRankServiceImpl.getAllUserRanks");

        List<UserRank> userRanks = userRankRepository.findAll();
        List<UserRankResponse> responses = userRanks.stream()
                .map(UserRankResponse::fromEntity)
                .toList();

        log.info("Inside UserRankServiceImpl.getAllUserRanks success count={}", responses.size());
        return responses;
    }
}