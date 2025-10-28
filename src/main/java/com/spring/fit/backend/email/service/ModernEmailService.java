package com.spring.fit.backend.email.service;

import java.io.ByteArrayOutputStream;

public interface ModernEmailService {
    void sendOrderConfirmationEmail(String to, String orderId, String orderDetails);
    void sendEmailWithAttachment(String to, String subject, String htmlContent, String attachmentName, ByteArrayOutputStream attachment);
    void sendSimpleEmail(String to, String subject, String htmlContent);
}

