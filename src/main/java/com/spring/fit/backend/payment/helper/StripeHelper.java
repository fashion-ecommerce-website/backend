package com.spring.fit.backend.payment.helper;

import com.spring.fit.backend.common.enums.VoucherType;
import com.spring.fit.backend.voucher.domain.entity.Voucher;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StripeHelper {

    public Session extractSession(Event event) {
        log.info("Extracting session from event type: {} with ID: {}", event.getType(), event.getId());
        
        try {
            // Method 1: Using EventDataObjectDeserializer
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isPresent()) {
                Session session = (Session) deserializer.getObject().get();
                log.info("Successfully extracted session using deserializer: {}", session.getId());
                return session;
            }
            
            // Method 2: Parse directly from rawJsonObject using reflection
            log.info("Deserializer object is null, trying rawJsonObject parsing...");
            try {
                // Use reflection to access rawJsonObject field
                java.lang.reflect.Field rawJsonField = deserializer.getClass().getDeclaredField("rawJsonObject");
                rawJsonField.setAccessible(true);
                com.google.gson.JsonObject rawJsonObject = (com.google.gson.JsonObject) rawJsonField.get(deserializer);
                
                if (rawJsonObject != null) {
                    log.info("Found rawJsonObject, parsing session directly...");
                    
                    // Parse the raw JSON object directly to Session
                    Session session = com.stripe.net.ApiResource.GSON.fromJson(rawJsonObject, Session.class);
                    if (session != null && session.getId() != null) {
                        log.info("Successfully parsed session from rawJsonObject: {}", session.getId());
                        return session;
                    }
                } else {
                    log.warn("RawJsonObject is null");
                }
            } catch (Exception e) {
                log.warn("Failed to parse from rawJsonObject using reflection: {}", e.getMessage());
            }
            
            // Method 3: Manual extraction from event JSON
            log.info("RawJsonObject parsing failed, trying manual extraction from event JSON...");
            String eventJson = event.toJson();
            log.debug("Event JSON: {}", eventJson);
            
            // Try to extract session ID from the event data
            if (eventJson.contains("\"object\":\"checkout.session\"")) {
                // Extract session ID from JSON
                String sessionId = extractSessionIdFromJson(eventJson);
                if (sessionId != null) {
                    log.info("Found session ID in JSON: {}", sessionId);
                    // Retrieve the session from Stripe API
                    try {
                        Session session = Session.retrieve(sessionId);
                        log.info("Successfully retrieved session from Stripe API: {}", session.getId());
                        return session;
                    } catch (StripeException e) {
                        log.error("Failed to retrieve session from Stripe API: {}", e.getMessage());
                    }
                }
            }
            
            // Method 4: Try to extract session data from event JSON directly
            log.info("Trying to extract session data from event JSON...");
            try {
                com.google.gson.JsonObject eventJsonObj = com.stripe.net.ApiResource.GSON.fromJson(eventJson, com.google.gson.JsonObject.class);
                if (eventJsonObj.has("data") && eventJsonObj.getAsJsonObject("data").has("object")) {
                    com.google.gson.JsonObject sessionData = eventJsonObj.getAsJsonObject("data").getAsJsonObject("object");
                    Session session = com.stripe.net.ApiResource.GSON.fromJson(sessionData, Session.class);
                    if (session != null && session.getId() != null) {
                        log.info("Successfully parsed session from event JSON: {}", session.getId());
                        return session;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse session from event JSON: {}", e.getMessage());
            }
            
            log.warn("All extraction methods failed for event: {}", event.getId());
            return null;
            
        } catch (Exception e) {
            log.error("Error extracting session from event {}: {}", event.getId(), e.getMessage(), e);
            return null;
        }
    }
    
    private String extractSessionIdFromJson(String eventJson) {
        try {
            // Simple JSON parsing to extract session ID
            // Look for "id": "cs_..." pattern
            int idIndex = eventJson.indexOf("\"id\":\"cs_");
            if (idIndex != -1) {
                int startIndex = idIndex + 6; // Skip "id":"
                int endIndex = eventJson.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return eventJson.substring(startIndex, endIndex);
                }
            }
        } catch (Exception e) {
            log.error("Error extracting session ID from JSON: {}", e.getMessage());
        }
        return null;
    }

    public void logEventDetails(Event event) {
        try {
            log.info("Event details - Type: {}, ID: {}, Created: {}", 
                event.getType(), event.getId(), event.getCreated());
            
            // Log event data structure using deserializer
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isPresent()) {
                Object dataObject = deserializer.getObject().get();
                log.info("Event data object type: {}", dataObject.getClass().getSimpleName());
                log.info("Data object: {}", dataObject.toString());
            } else {
                log.warn("Could not deserialize event data object");
                
                // Log rawJsonObject details using reflection
                try {
                    java.lang.reflect.Field rawJsonField = deserializer.getClass().getDeclaredField("rawJsonObject");
                    rawJsonField.setAccessible(true);
                    com.google.gson.JsonObject rawJsonObject = (com.google.gson.JsonObject) rawJsonField.get(deserializer);
                    
                    if (rawJsonObject != null) {
                        log.info("RawJsonObject found: {}", rawJsonObject.toString());
                        if (rawJsonObject.has("id")) {
                            log.info("RawJsonObject contains session ID: {}", rawJsonObject.get("id").getAsString());
                        }
                        if (rawJsonObject.has("metadata")) {
                            log.info("RawJsonObject contains metadata: {}", rawJsonObject.get("metadata").toString());
                        }
                    } else {
                        log.warn("RawJsonObject is also null");
                    }
                } catch (Exception e) {
                    log.warn("Error accessing rawJsonObject via reflection: {}", e.getMessage());
                }
            }
            
            // Log raw JSON for debugging
            String eventJson = event.toJson();
            log.debug("Event JSON: {}", eventJson);
            
        } catch (Exception e) {
            log.error("Error logging event details: {}", e.getMessage(), e);
        }
    }

    public boolean validateSessionMetadata(Session session) {
        if (session == null) {
            log.warn("Session is null");
            return false;
        }
        
        if (session.getMetadata() == null) {
            log.warn("Session metadata is null for session: {}", session.getId());
            return false;
        }
        
        String orderId = session.getMetadata().get("orderId");
        if (orderId == null || orderId.trim().isEmpty()) {
            log.warn("No orderId found in session metadata: {}", session.getMetadata());
            return false;
        }
        
        log.info("Session metadata validation passed for session: {} with orderId: {}", session.getId(), orderId);
        return true;
    }

    public Long extractOrderIdFromSession(Session session) {
        if (!validateSessionMetadata(session)) {
            return null;
        }
        
        try {
            String orderIdStr = session.getMetadata().get("orderId");
            Long orderId = Long.parseLong(orderIdStr);
            log.info("Extracted order ID: {} from session: {}", orderId, session.getId());
            return orderId;
        } catch (NumberFormatException e) {
            log.warn("Invalid orderId in session metadata: {}", session.getMetadata().get("orderId"));
            return null;
        }
    }

    public String createDiscountCoupon(Long discountAmountCents, String currency) {
        try {
            com.stripe.param.CouponCreateParams couponParams = com.stripe.param.CouponCreateParams.builder()
                    .setAmountOff(discountAmountCents)
                    .setCurrency(currency.toLowerCase())
                    .setDuration(com.stripe.param.CouponCreateParams.Duration.ONCE)
                    .setName("Order Discount")
                    .build();

            com.stripe.model.Coupon coupon = com.stripe.model.Coupon.create(couponParams);
            log.info("Created Stripe coupon: {} for discount: {} cents", coupon.getId(), discountAmountCents);
            return coupon.getId();
        } catch (StripeException e) {
            log.error("Failed to create Stripe coupon for discount: {}", e.getMessage(), e);
            return null;
        }
    }

    public Long convertToCents(java.math.BigDecimal amount) {
        return amount.movePointRight(0).longValue();
    }

    public String formatCurrency(java.math.BigDecimal amount) {
        return String.format("%,.0f", amount);
    }

    public BigDecimal calculateVoucherDiscount(Voucher voucher, BigDecimal subtotalAmount) {
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        if (voucher.getType() == VoucherType.PERCENT) {
            // Calculate percentage discount
            discountAmount = subtotalAmount.multiply(voucher.getValue().divide(BigDecimal.valueOf(100)));
            
            // Apply max discount limit if set
            if (voucher.getMaxDiscount() != null && discountAmount.compareTo(voucher.getMaxDiscount()) > 0) {
                discountAmount = voucher.getMaxDiscount();
            }
        } else if (voucher.getType() == VoucherType.FIXED) {
            // Fixed amount discount
            discountAmount = voucher.getValue();
        }
        
        // Ensure discount doesn't exceed subtotal
        if (discountAmount.compareTo(subtotalAmount) > 0) {
            discountAmount = subtotalAmount;
        }
        
        return discountAmount;
    }
}