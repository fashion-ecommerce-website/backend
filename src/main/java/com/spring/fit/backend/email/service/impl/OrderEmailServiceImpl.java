package com.spring.fit.backend.email.service.impl;

import com.spring.fit.backend.email.service.ModernEmailService;
import com.spring.fit.backend.email.service.OrderEmailService;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.repository.OrderRepository;
import com.spring.fit.backend.common.exception.ErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEmailServiceImpl implements OrderEmailService {

    private final ModernEmailService modernEmailService;

    @Override
    public void sendOrderDetailsEmail(Order order) {
        log.info("Sending order details email for order: {} to user: {}", order.getId(), order.getUser().getEmail());
        
        try {
            String orderDetailsHtml = generateOrderDetailsHtml(order);
            modernEmailService.sendOrderConfirmationEmail(
                order.getUser().getEmail(), 
                order.getId().toString(), 
                orderDetailsHtml
            );
            
            log.info("Order details email sent successfully for order: {}", order.getId());
            
        } catch (Exception e) {
            log.error("Failed to send order details email for order {}: {}", order.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send order details email", e);
        }
    }

    private String generateOrderDetailsHtml(Order order) {
        StringBuilder details = new StringBuilder();
        
        // Order summary
        details.append("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0;'>");
        details.append("<h4 style='margin: 0 0 10px 0; color: #495057;'>💰 Tổng quan đơn hàng</h4>");
        details.append("<p style='margin: 5px 0;'><strong>Tổng tiền hàng:</strong> ").append(formatCurrency(order.getSubtotalAmount())).append(" VND</p>");
        details.append("<p style='margin: 5px 0;'><strong>Phí vận chuyển:</strong> ").append(formatCurrency(order.getShippingFee())).append(" VND</p>");
        if (order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            details.append("<p style='margin: 5px 0; color: #28a745;'><strong>Giảm giá:</strong> -").append(formatCurrency(order.getDiscountAmount())).append(" VND</p>");
        }
        details.append("<p style='margin: 5px 0; font-size: 18px; font-weight: bold; color: #dc3545;'><strong>Tổng cộng:</strong> ").append(formatCurrency(order.getTotalAmount())).append(" VND</p>");
        details.append("</div>");

        // Order items
        details.append("<h4 style='color: #495057; margin: 20px 0 10px 0;'>🛍️ Sản phẩm đã đặt</h4>");
        details.append("<div style='border: 1px solid #dee2e6; border-radius: 8px; overflow: hidden;'>");
        
        if (order.getOrderDetails().isEmpty()) {
            details.append("<p style='padding: 20px; text-align: center; color: #6c757d;'>Không có sản phẩm nào</p>");
        } else {
            details.append("<table style='width: 100%; border-collapse: collapse;'>");
            details.append("<thead style='background-color: #e9ecef;'>");
            details.append("<tr>");
            details.append("<th style='padding: 12px; text-align: left; border-bottom: 1px solid #dee2e6;'>Sản phẩm</th>");
            details.append("<th style='padding: 12px; text-align: center; border-bottom: 1px solid #dee2e6;'>Số lượng</th>");
            details.append("<th style='padding: 12px; text-align: right; border-bottom: 1px solid #dee2e6;'>Đơn giá</th>");
            details.append("<th style='padding: 12px; text-align: right; border-bottom: 1px solid #dee2e6;'>Thành tiền</th>");
            details.append("</tr>");
            details.append("</thead>");
            details.append("<tbody>");
            
            order.getOrderDetails().forEach(detail -> {
                details.append("<tr style='border-bottom: 1px solid #f8f9fa;'>");
                details.append("<td style='padding: 12px;'>");
                details.append("<div>");
                details.append("<strong>").append(escapeHtml(detail.getTitle())).append("</strong><br>");
                details.append("<small style='color: #6c757d;'>Màu: ").append(escapeHtml(detail.getColorLabel())).append(" | Size: ").append(escapeHtml(detail.getSizeLabel())).append("</small>");
                details.append("</div>");
                details.append("</td>");
                details.append("<td style='padding: 12px; text-align: center;'>").append(detail.getQuantity()).append("</td>");
                details.append("<td style='padding: 12px; text-align: right;'>").append(formatCurrency(detail.getUnitPrice())).append(" VND</td>");
                details.append("<td style='padding: 12px; text-align: right; font-weight: bold;'>").append(formatCurrency(detail.getTotalPrice())).append(" VND</td>");
                details.append("</tr>");
            });
            
            details.append("</tbody>");
            details.append("</table>");
        }
        details.append("</div>");

        // Shipping address
        if (order.getShippingAddress() != null) {
            details.append("<h4 style='color: #495057; margin: 20px 0 10px 0;'>📍 Địa chỉ giao hàng</h4>");
            details.append("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 8px; border-left: 4px solid #28a745;'>");
            details.append("<p style='margin: 5px 0;'><strong>Người nhận:</strong> ").append(escapeHtml(order.getShippingAddress().getFullName())).append("</p>");
            details.append("<p style='margin: 5px 0;'><strong>Số điện thoại:</strong> ").append(escapeHtml(order.getShippingAddress().getPhone())).append("</p>");
            details.append("<p style='margin: 5px 0;'><strong>Địa chỉ:</strong> ").append(escapeHtml(order.getShippingAddress().getLine())).append("</p>");
            details.append("<p style='margin: 5px 0;'><strong>Phường/Xã:</strong> ").append(escapeHtml(order.getShippingAddress().getWard())).append("</p>");
            details.append("<p style='margin: 5px 0;'><strong>Thành phố:</strong> ").append(escapeHtml(order.getShippingAddress().getCity())).append("</p>");
            details.append("</div>");
        }

        // Payment information
        if (!order.getPayments().isEmpty()) {
            details.append("<h4 style='color: #495057; margin: 20px 0 10px 0;'>💳 Thông tin thanh toán</h4>");
            details.append("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 8px;'>");
            order.getPayments().forEach(payment -> {
                details.append("<p style='margin: 5px 0;'><strong>Phương thức:</strong> ").append(escapeHtml(payment.getMethod().name())).append("</p>");
                details.append("<p style='margin: 5px 0;'><strong>Trạng thái:</strong> ").append(getPaymentStatusText(payment.getStatus().name())).append("</p>");
                details.append("<p style='margin: 5px 0;'><strong>Số tiền:</strong> ").append(formatCurrency(payment.getAmount())).append(" VND</p>");
                if (payment.getTransactionNo() != null) {
                    details.append("<p style='margin: 5px 0;'><strong>Mã giao dịch:</strong> ").append(escapeHtml(payment.getTransactionNo())).append("</p>");
                }
            });
            details.append("</div>");
        }

        // Order notes
        if (order.getNote() != null && !order.getNote().trim().isEmpty()) {
            details.append("<h4 style='color: #495057; margin: 20px 0 10px 0;'>📝 Ghi chú đơn hàng</h4>");
            details.append("<div style='background-color: #fff3cd; padding: 15px; border-radius: 8px; border-left: 4px solid #ffc107;'>");
            details.append("<p style='margin: 0;'>").append(escapeHtml(order.getNote())).append("</p>");
            details.append("</div>");
        }

        return details.toString();
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f", amount);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    private String getPaymentStatusText(String status) {
        switch (status) {
            case "UNPAID": return "<span style='color: #dc3545; font-weight: bold;'>Chưa thanh toán</span>";
            case "PAID": return "<span style='color: #28a745; font-weight: bold;'>Đã thanh toán</span>";
            case "REFUNDED": return "<span style='color: #6c757d; font-weight: bold;'>Đã hoàn tiền</span>";
            default: return status;
        }
    }
}
