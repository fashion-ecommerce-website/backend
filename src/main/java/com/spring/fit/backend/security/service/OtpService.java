package com.spring.fit.backend.security.service;

public interface OtpService {
    
    /**
     * Send OTP to phone number
     */
    void sendOtp(String phone);
    
    /**
     * Verify OTP code
     */
    boolean verifyOtp(String phone, String otpCode);
}

