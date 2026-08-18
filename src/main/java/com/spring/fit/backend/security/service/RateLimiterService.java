package com.spring.fit.backend.security.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RateLimiterService {

    // OTP Rate Limiting: max 1 OTP request per 60 seconds per email
    private static final long OTP_COOLDOWN_SECONDS = 60;
    private final Map<String, Instant> otpLastRequestTime = new ConcurrentHashMap<>();

    // Login Rate Limiting: max 5 failed attempts per 15 minutes per email
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOGIN_LOCK_PERIOD_SECONDS = 15 * 60; // 15 minutes
    private final Map<String, FailedAttemptInfo> failedLoginAttempts = new ConcurrentHashMap<>();

    private static class FailedAttemptInfo {
        int count;
        Instant lockUntil;

        FailedAttemptInfo(int count, Instant lockUntil) {
            this.count = count;
            this.lockUntil = lockUntil;
        }
    }

    /**
     * Checks if OTP request is allowed for the given email (cooldown 60 seconds)
     */
    public boolean allowOtpRequest(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String key = email.trim().toLowerCase();
        Instant now = Instant.now();
        Instant lastTime = otpLastRequestTime.get(key);

        if (lastTime != null && now.isBefore(lastTime.plusSeconds(OTP_COOLDOWN_SECONDS))) {
            log.warn("Rate limit exceeded for OTP request on email={}", email);
            return false;
        }

        otpLastRequestTime.put(key, now);
        return true;
    }

    /**
     * Gets seconds remaining before next OTP can be requested
     */
    public long getOtpCooldownRemainingSeconds(String email) {
        if (email == null) return 0;
        String key = email.trim().toLowerCase();
        Instant lastTime = otpLastRequestTime.get(key);
        if (lastTime == null) return 0;
        long elapsed = Instant.now().getEpochSecond() - lastTime.getEpochSecond();
        return Math.max(0, OTP_COOLDOWN_SECONDS - elapsed);
    }

    /**
     * Checks if login is locked out due to too many failed attempts
     */
    public boolean isLoginLocked(String email) {
        if (email == null) return false;
        String key = email.trim().toLowerCase();
        FailedAttemptInfo info = failedLoginAttempts.get(key);
        if (info == null) return false;

        if (info.lockUntil != null && Instant.now().isBefore(info.lockUntil)) {
            log.warn("Login attempt blocked due to rate limiting lockout for email={}", email);
            return true;
        }

        // Lock expired
        if (info.lockUntil != null && Instant.now().isAfter(info.lockUntil)) {
            failedLoginAttempts.remove(key);
        }

        return false;
    }

    /**
     * Records a failed login attempt for the given email
     */
    public void recordFailedLogin(String email) {
        if (email == null) return;
        String key = email.trim().toLowerCase();
        Instant now = Instant.now();

        failedLoginAttempts.compute(key, (k, existing) -> {
            if (existing == null) {
                return new FailedAttemptInfo(1, null);
            }
            int newCount = existing.count + 1;
            if (newCount >= MAX_FAILED_ATTEMPTS) {
                log.warn("Max login failures reached for email={}. Locking out for 15 minutes.", email);
                return new FailedAttemptInfo(newCount, now.plusSeconds(LOGIN_LOCK_PERIOD_SECONDS));
            }
            return new FailedAttemptInfo(newCount, null);
        });
    }

    /**
     * Resets failed login count on successful login
     */
    public void resetFailedLogin(String email) {
        if (email == null) return;
        failedLoginAttempts.remove(email.trim().toLowerCase());
    }
}
