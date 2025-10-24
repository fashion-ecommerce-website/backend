package com.spring.fit.backend.security.service;

import jakarta.mail.MessagingException;
import java.io.ByteArrayOutputStream;

public interface EmailService {
    void sendEmail(String to, String subject, String text) throws MessagingException;
    void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, ByteArrayOutputStream attachment) throws MessagingException;
}
