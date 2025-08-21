package com.spring.fit.backend.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateLimitException extends RuntimeException {

    private final int status;
    private final String error;
    private final int retryAfter;
    private final int hourlyLimit;
    private final int currentUsage;

    public RateLimitException(int retryAfter, int hourlyLimit, int currentUsage) {
        super("Rate limit exceeded. Please try again after 3600 seconds.");
        this.status = 429;
        this.error = "Too Many Requests";
        this.retryAfter = retryAfter;
        this.hourlyLimit = hourlyLimit;
        this.currentUsage = currentUsage;
    }
}