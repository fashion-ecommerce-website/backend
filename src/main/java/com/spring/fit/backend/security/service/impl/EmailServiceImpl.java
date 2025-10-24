package com.spring.fit.backend.security.service.impl;

import com.spring.fit.backend.security.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@fitapp.com");
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    @Async
    public void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, ByteArrayOutputStream attachment) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); // true indicates HTML content
            helper.setFrom("noreply@fitapp.com");
            
            // Add attachment
            helper.addAttachment(attachmentName, new jakarta.mail.util.ByteArrayDataSource(attachment.toByteArray(), "application/pdf"));
            
            mailSender.send(message);
            log.info("Email with attachment sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email with attachment to {}: {}", to, e.getMessage(), e);
            throw new MessagingException("Failed to send email with attachment", e);
        }
    }
}
