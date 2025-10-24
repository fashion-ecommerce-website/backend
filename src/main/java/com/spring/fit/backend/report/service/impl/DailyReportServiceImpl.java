package com.spring.fit.backend.report.service.impl;

import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.repository.OrderRepository;
import com.spring.fit.backend.report.service.DailyReportService;
import com.spring.fit.backend.report.service.PdfReportService;
import com.spring.fit.backend.security.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyReportServiceImpl implements DailyReportService {

    private final OrderRepository orderRepository;
    private final PdfReportService pdfReportService;
    private final EmailService emailService;

    @Override
    public void sendDailyReport(String email) {
        log.info("Generating daily report for email: {}", email);
        
        try {
            // Get today's orders
            List<Order> todayOrders = getTodayOrders();
            log.info("Found {} orders for today", todayOrders.size());
            
            // Generate PDF
            var pdfStream = pdfReportService.generateDailyReportPdf(todayOrders);
            
            // Prepare email content
            String subject = "Báo cáo hàng ngày - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String emailContent = generateEmailContent(todayOrders.size());
            String attachmentName = "daily_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
            
            // Send email with PDF attachment
            emailService.sendEmailWithAttachment(email, subject, emailContent, attachmentName, pdfStream);
            
            log.info("Daily report sent successfully to: {}", email);
            
        } catch (MessagingException e) {
            log.error("Failed to send daily report to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send daily report", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending daily report to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send daily report", e);
        }
    }

    private List<Order> getTodayOrders() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        // Use Pageable.unpaged() to get all results without pagination
        return orderRepository.findByDateRange(startOfDay, endOfDay, org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    private String generateEmailContent(int orderCount) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        return String.format("""
            <html>
            <body>
                <h2>Báo cáo hàng ngày - %s</h2>
                <p>Xin chào,</p>
                <p>Đây là báo cáo hàng ngày của hệ thống Fit App.</p>
                <p><strong>Tổng số đơn hàng hôm nay:</strong> %d</p>
                <p>Chi tiết báo cáo được đính kèm trong file PDF.</p>
                <p>Trân trọng,<br>Hệ thống Fit App</p>
            </body>
            </html>
            """, dateStr, orderCount);
    }
}
