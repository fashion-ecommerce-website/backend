package com.spring.fit.backend.order.controller;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.domain.entity.Shipment;
import com.spring.fit.backend.order.repository.ShipmentRepository;
import com.spring.fit.backend.order.service.TrackingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Webhook controller để nhận tracking updates từ carriers (GHTK, GHN, etc.)
 * 
 * Note: Webhook endpoint phải accessible từ internet (HTTPS recommended)
 * Cần đăng ký webhook URL trong GHTK dashboard
 */
@RestController
@RequestMapping("/api/webhooks/tracking")
@RequiredArgsConstructor
@Slf4j
public class TrackingWebhookController {

    private final TrackingService trackingService;
    private final ShipmentRepository shipmentRepository;

    @Value("${carrier.ghn.webhook-secret:}")
    private String webhookSecret; 

    /**
     * Webhook endpoint để nhận tracking updates từ GHN
     * 
     * GHN sẽ gửi POST request đến endpoint này khi có status update
     * 
     * @param payload Webhook payload từ GHN
     * @param request HTTP request để log IP address
     * @return Response để acknowledge webhook
     */
    @PostMapping("/ghn")
    public ResponseEntity<Map<String, Object>> handleGHNWebhook(
            @RequestBody GHNWebhookPayload payload,
            @RequestHeader(value = "X-GHN-Signature", required = false) String signature,
            HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        log.info("Received GHN webhook from IP {}: orderCode={}, status={}", 
                clientIp, payload.getOrderCode(), payload.getStatus());

        // Validate payload
        if (payload.getOrderCode() == null || payload.getOrderCode().isBlank()) {
            log.warn("Invalid GHN webhook: missing orderCode");
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Missing orderCode"));
        }

        // Optional: Validate signature if configured
        if (StringUtils.hasText(webhookSecret) && StringUtils.hasText(signature)) {
            if (!validateSignature(payload, signature)) {
                log.warn("Invalid GHN webhook signature from IP {}", clientIp);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid signature"));
            }
        }

        try {
            Shipment shipment = shipmentRepository.findByTrackingNo(payload.getOrderCode())
                    .orElseThrow(() -> {
                        log.warn("Shipment not found for tracking number: {}", payload.getOrderCode());
                        return new ErrorException(HttpStatus.NOT_FOUND,
                                "Shipment not found for tracking: " + payload.getOrderCode());
                    });

            // Update tracking status immediately
            trackingService.refreshTracking(shipment.getId());
            
            log.info("Successfully processed GHN webhook for shipment {}", shipment.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Webhook processed"));
            
        } catch (ErrorException ex) {
            // Known errors - return appropriate status
            log.error("Error processing GHN webhook: {}", ex.getMessage());
            return ResponseEntity.status(ex.getStatus())
                    .body(Map.of("success", false, "message", ex.getMessage()));
        } catch (Exception e) {
            // Unknown errors
            log.error("Unexpected error processing GHN webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Internal server error"));
        }
    }

    /**
     * Validate webhook signature (if GHN provides signature validation)
     */
    private boolean validateSignature(GHNWebhookPayload payload, String signature) {
        if (!StringUtils.hasText(webhookSecret)) {
            return true; // Skip validation if secret not configured
        }
        
        // Example: HMAC-SHA256 validation (adjust based on GHN's actual method)
        // return signature.equals(expectedSignature);
        
        return true; // For now, accept all if signature header exists
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Data
    private static class GHNWebhookPayload {
        private String orderCode;    // Tracking number (order_code)
        private String status;       // GHN status
        private String location;     // Optional: current location
        private String time;         // Optional: event time
    }
}

