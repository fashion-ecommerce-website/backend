package com.spring.fit.backend.security.service;

public interface EmailOtpService {
    
    void sendOtp(String email);
    
    boolean verifyOtp(String email, String otpCode);
}
