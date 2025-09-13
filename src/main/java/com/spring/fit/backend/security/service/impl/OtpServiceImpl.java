package com.spring.fit.backend.security.service.impl;

import org.springframework.stereotype.Service;

import com.spring.fit.backend.security.service.OtpService;
import com.spring.fit.backend.security.service.EmailOtpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final EmailOtpService emailOtpService;

    @Override
    public void sendOtp(String email) {
        log.info("Sending OTP to email: {}", email);
        emailOtpService.sendOtp(email);
    }

    @Override
    public boolean verifyOtp(String email, String otpCode) {
        log.info("Verifying OTP for email: {}", email);
        return emailOtpService.verifyOtp(email, otpCode);
    }
}