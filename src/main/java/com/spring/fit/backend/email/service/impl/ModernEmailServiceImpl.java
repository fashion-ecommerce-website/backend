package com.spring.fit.backend.email.service.impl;

import com.spring.fit.backend.email.service.ModernEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModernEmailServiceImpl implements ModernEmailService {

    @Value("${spring.mail.host}")
    private String smtpHost;

    @Value("${spring.mail.port}")
    private int smtpPort;

    @Value("${spring.mail.username}")
    private String smtpUsername;

    @Value("${spring.mail.password}")
    private String smtpPassword;

    @Value("${app.email.from}")
    private String fromEmail;

    private Mailer getMailer() {
        return MailerBuilder
                .withSMTPServer(smtpHost, smtpPort, smtpUsername, smtpPassword)
                .withSessionTimeout(10 * 1000)
                .withTransportStrategy(org.simplejavamail.api.mailer.config.TransportStrategy.SMTP_TLS)
                .buildMailer();
    }

    @Override
    public void sendOrderConfirmationEmail(String to, String orderId, String orderDetails) {
        log.info("Sending order confirmation email to: {} for order: {}", to, orderId);
        
        try {
            String subject = "Xác nhận đơn hàng #" + orderId + " - Fit App";
            String htmlContent = createOrderConfirmationTemplate(orderId, orderDetails);
            
            Email email = EmailBuilder.startingBlank()
                    .from("Fit App", fromEmail)
                    .to(to)
                    .withSubject(subject)
                    .withHTMLText(htmlContent)
                    .buildEmail();

            getMailer().sendMail(email);
            log.info("Order confirmation email sent successfully to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send order confirmation email", e);
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String htmlContent, String attachmentName, ByteArrayOutputStream attachment) {
        log.info("Sending email with attachment to: {}", to);
        
        try {
            Email email = EmailBuilder.startingBlank()
                    .from("Fit App", fromEmail)
                    .to(to)
                    .withSubject(subject)
                    .withHTMLText(htmlContent)
                    .withAttachment(attachmentName, attachment.toByteArray(), "application/pdf")
                    .buildEmail();

            getMailer().sendMail(email);
            log.info("Email with attachment sent successfully to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send email with attachment to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String htmlContent) {
        log.info("Sending simple email to: {}", to);
        
        try {
            Email email = EmailBuilder.startingBlank()
                    .from("Fit App", fromEmail)
                    .to(to)
                    .withSubject(subject)
                    .withHTMLText(htmlContent)
                    .buildEmail();

            getMailer().sendMail(email);
            log.info("Simple email sent successfully to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send simple email", e);
        }
    }

    private String createOrderConfirmationTemplate(String orderId, String orderDetails) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác nhận đơn hàng</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 20px rgba(0,0,0,0.1); }
                    .header { text-align: center; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; border-radius: 10px 10px 0 0; margin: -20px -20px 30px -20px; }
                    .header h1 { margin: 0; font-size: 28px; font-weight: 300; }
                    .order-info { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #667eea; }
                    .order-details { background-color: #ffffff; border: 1px solid #e9ecef; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; padding: 20px; background-color: #f8f9fa; border-radius: 8px; color: #6c757d; }
                    .btn { display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; margin: 10px 0; }
                    .highlight { color: #667eea; font-weight: bold; }
                    .status { display: inline-block; background-color: #28a745; color: white; padding: 5px 15px; border-radius: 20px; font-size: 12px; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Đơn hàng của bạn đã được xác nhận!</h1>
                        <p>Cảm ơn bạn đã tin tưởng và mua sắm tại Fit App</p>
                    </div>
                    
                    <div class="order-info">
                        <h2>📋 Thông tin đơn hàng</h2>
                        <p><strong>Mã đơn hàng:</strong> <span class="highlight">#%s</span></p>
                        <p><strong>Trạng thái:</strong> <span class="status">Đã xác nhận</span></p>
                        <p><strong>Thời gian đặt hàng:</strong> %s</p>
                    </div>
                    
                    <div class="order-details">
                        <h3>📦 Chi tiết đơn hàng</h3>
                        %s
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="#" class="btn">Theo dõi đơn hàng</a>
                        <a href="#" class="btn" style="background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%);">Tiếp tục mua sắm</a>
                    </div>
                    
                    <div class="footer">
                        <p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi:</p>
                        <p>📧 Email: support@fitapp.com | 📞 Hotline: 1900-xxxx</p>
                        <p>© 2024 Fit App. Tất cả quyền được bảo lưu.</p>
                    </div>
                </div>
            </body>
            </html>
            """, orderId, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), orderDetails);
    }
}

