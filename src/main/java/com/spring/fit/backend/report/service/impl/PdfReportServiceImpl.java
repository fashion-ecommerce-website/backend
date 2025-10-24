package com.spring.fit.backend.report.service.impl;

import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.report.service.PdfReportService;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.layout.font.FontProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportServiceImpl implements PdfReportService {

    @Override
    public ByteArrayOutputStream generateDailyReportPdf(List<Order> orders) {
        log.info("Generating daily report PDF for {} orders", orders.size());
        
        // Create a fresh output stream for each call
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            // Use simple HTML to avoid complex rendering issues
            String simpleHtml = createSimpleHtml(
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
                orders.size(), 
                orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
            );
            
            log.debug("Using simple HTML for PDF generation, length: {}", simpleHtml.length());
            
            // Create fresh converter properties for each call
            ConverterProperties converterProperties = new ConverterProperties();
            FontProvider fontProvider = new DefaultFontProvider(true, true, true);
            converterProperties.setFontProvider(fontProvider);
            converterProperties.setBaseUri("file:///");
            
            // Generate PDF
            HtmlConverter.convertToPdf(simpleHtml, outputStream, converterProperties);
            
            log.info("PDF generated successfully, size: {} bytes", outputStream.size());
            return outputStream;
            
        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
            
            // Close stream on error
            try {
                outputStream.close();
            } catch (IOException closeEx) {
                log.warn("Failed to close output stream: {}", closeEx.getMessage());
            }
            
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    
    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f", amount);
    }
    
    
    private String createSimpleHtml(String dateStr, int totalOrders, BigDecimal totalRevenue) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>Daily Report - %s</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    h1 { color: #333; text-align: center; }
                    .summary { background-color: #e8f4fd; padding: 15px; margin: 20px 0; border-radius: 5px; }
                </style>
            </head>
            <body>
                <h1>Báo cáo hàng ngày - %s</h1>
                <div class='summary'>
                    <h3>Tổng quan</h3>
                    <p><strong>Tổng số đơn hàng:</strong> %d</p>
                    <p><strong>Tổng doanh thu:</strong> %s VND</p>
                </div>
                <p>Không có đơn hàng nào trong ngày hôm nay.</p>
            </body>
            </html>
            """, dateStr, dateStr, totalOrders, formatCurrency(totalRevenue));
    }
    
    @Override
    public ByteArrayOutputStream testSimplePdfGeneration() {
        log.info("Testing simple PDF generation");
        
        ByteArrayOutputStream outputStream = null;
        try {
            String simpleHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset='UTF-8'>
                    <title>Test PDF</title>
                </head>
                <body>
                    <h1>Test PDF Generation</h1>
                    <p>This is a simple test PDF.</p>
                </body>
                </html>
                """;
            
            outputStream = new ByteArrayOutputStream();
            
            // Configure converter properties
            ConverterProperties converterProperties = new ConverterProperties();
            FontProvider fontProvider = new DefaultFontProvider(true, true, true);
            converterProperties.setFontProvider(fontProvider);
            converterProperties.setBaseUri("file:///");
            
            HtmlConverter.convertToPdf(simpleHtml, outputStream, converterProperties);
            
            log.info("Simple PDF generated successfully, size: {} bytes", outputStream.size());
            return outputStream;
            
        } catch (Exception e) {
            log.error("Error generating simple PDF: {}", e.getMessage(), e);
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException closeEx) {
                    log.warn("Failed to close output stream: {}", closeEx.getMessage());
                }
            }
            throw new RuntimeException("Failed to generate simple PDF", e);
        }
    }
}
