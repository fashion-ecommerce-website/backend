package com.spring.fit.backend.security.service;

public interface SmsService {
    
    /**
     * Send SMS message to phone number
     */
    void sendSms(String phoneNumber, String message);
}

