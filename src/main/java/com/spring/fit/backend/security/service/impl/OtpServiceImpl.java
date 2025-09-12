package com.spring.fit.backend.security.service.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.spring.fit.backend.security.service.OtpService;
import com.spring.fit.backend.security.service.SmsService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OtpServiceImpl implements OtpService {

    // In-memory storage for OTPs (phone -> {code, expiresAt})
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final SmsService smsService;
    
    // OTP expiry time in minutes
    private static final int OTP_EXPIRY_MINUTES = 5;
    
    // OTP length
    private static final int OTP_LENGTH = 6;

    public OtpServiceImpl(SmsService smsService) {
        this.smsService = smsService;
    }

    @Override
    public void sendOtp(String phone) {
        log.info("Sending OTP to phone: {}", phone);
        
        // Generate 6-digit OTP
        String otpCode = generateOtp();
        
        // Set expiry time
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        
        // Store OTP
        otpStorage.put(phone, new OtpData(otpCode, expiresAt));
        
        // Send SMS with OTP
        String message = String.format("Your OTP code is: %s. This code will expire in %d minutes.", 
                                     otpCode, OTP_EXPIRY_MINUTES);
        
        try {
            smsService.sendSms(phone, message);
            log.info("OTP sent successfully to phone: {}", phone);
        } catch (Exception e) {
            log.error("Failed to send OTP to phone {}: {}", phone, e.getMessage());
            // Remove OTP from storage if SMS sending failed
            otpStorage.remove(phone);
            throw new RuntimeException("Failed to send OTP: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyOtp(String phone, String otpCode) {
        log.info("Verifying OTP for phone: {}", phone);
        
        OtpData otpData = otpStorage.get(phone);
        
        if (otpData == null) {
            log.warn("No OTP found for phone: {}", phone);
            return false;
        }
        
        if (otpData.expiresAt.isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for phone: {}", phone);
            otpStorage.remove(phone); // Clean up expired OTP
            return false;
        }
        
        if (!otpData.code.equals(otpCode)) {
            log.warn("Invalid OTP for phone: {}", phone);
            return false;
        }
        
        // OTP is valid, remove it from storage
        otpStorage.remove(phone);
        log.info("OTP verified successfully for phone: {}", phone);
        return true;
    }
    
    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
    
    // Inner class to store OTP data
    private static class OtpData {
        final String code;
        final LocalDateTime expiresAt;
        
        OtpData(String code, LocalDateTime expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }
}
