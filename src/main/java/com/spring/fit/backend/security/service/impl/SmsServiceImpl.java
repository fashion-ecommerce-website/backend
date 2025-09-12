package com.spring.fit.backend.security.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spring.fit.backend.security.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    public SmsServiceImpl() {
        // Initialize Twilio (will be set when properties are loaded)
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        try {
            // Initialize Twilio with credentials
            Twilio.init(accountSid, authToken);

            // Format phone number (ensure it starts with +)
            String formattedPhone = formatPhoneNumber(phoneNumber);

            // Send SMS
            Message twilioMessage = Message.creator(
                    new PhoneNumber(formattedPhone), // to
                    new PhoneNumber(fromPhoneNumber), // from
                    message
            ).create();

            log.info("SMS sent successfully to {} with SID: {}", formattedPhone, twilioMessage.getSid());
            
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Remove any non-digit characters
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");
        
        // Add country code if not present (assuming Vietnam +84)
        if (digitsOnly.startsWith("0")) {
            // Replace leading 0 with +84
            return "+84" + digitsOnly.substring(1);
        } else if (!digitsOnly.startsWith("84")) {
            // Add +84 if no country code
            return "+84" + digitsOnly;
        } else {
            // Already has country code, just add +
            return "+" + digitsOnly;
        }
    }
}

