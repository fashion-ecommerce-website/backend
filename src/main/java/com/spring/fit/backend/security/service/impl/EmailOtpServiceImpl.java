package com.spring.fit.backend.security.service.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.spring.fit.backend.security.service.EmailOtpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailOtpServiceImpl implements EmailOtpService {

    private final JavaMailSender mailSender;

    // In-memory storage for OTPs (email -> {code, expiresAt})
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    
    // OTP expiry time in minutes
    private static final int OTP_EXPIRY_MINUTES = 5;
    
    // OTP length
    private static final int OTP_LENGTH = 6;

    @Override
    public void sendOtp(String email) {
        log.info("Sending OTP to email: {}", email);
        
        // Generate 6-digit OTP
        String otpCode = generateOtp();
        
        // Set expiry time
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        
        // Store OTP
        otpStorage.put(email, new OtpData(otpCode, expiresAt));
        
        // Send email with OTP
        String subject = "Your OTP Code - Fit App";
        String message = String.format(
            "Your OTP code is: %s\n\n" +
            "This code will expire in %d minutes.\n\n" +
            "If you didn't request this code, please ignore this email.\n\n" +
            "Best regards,\nFit App Team",
            otpCode, OTP_EXPIRY_MINUTES
        );
        
        try {
            sendEmail(email, subject, message);
            log.info("OTP sent successfully to email: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP to email {}: {}", email, e.getMessage());
            // Remove OTP from storage if email sending failed
            otpStorage.remove(email);
            throw new RuntimeException("Failed to send OTP: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyOtp(String email, String otpCode) {
        log.info("Verifying OTP for email: {}", email);
        
        OtpData otpData = otpStorage.get(email);
        
        if (otpData == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }
        
        if (otpData.expiresAt.isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for email: {}", email);
            otpStorage.remove(email); // Clean up expired OTP
            return false;
        }
        
        if (!otpData.code.equals(otpCode)) {
            log.warn("Invalid OTP for email: {}", email);
            return false;
        }
        
        // OTP is valid, remove it from storage
        otpStorage.remove(email);
        log.info("OTP verified successfully for email: {}", email);
        return true;
    }
    
    private void sendEmail(String to, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailMessage.setFrom("noreply@fitapp.com");
        
        mailSender.send(mailMessage);
        log.info("Email sent successfully to: {}", to);
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
