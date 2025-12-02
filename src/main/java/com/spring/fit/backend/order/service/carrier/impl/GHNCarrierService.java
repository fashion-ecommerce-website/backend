package com.spring.fit.backend.order.service.carrier.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spring.fit.backend.common.enums.OrderStatus;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.domain.entity.Shipment;
import com.spring.fit.backend.order.domain.dto.tracking.TrackingEventData;
import com.spring.fit.backend.order.domain.dto.tracking.TrackingResponse;
import com.spring.fit.backend.order.service.carrier.CarrierService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GHNCarrierService implements CarrierService {

    private static final String DEFAULT_BASE_URL = "https://dev-online-gateway.ghn.vn";
    private static final DateTimeFormatter GHN_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

    private final RestTemplate restTemplate;

    @Value("${carrier.ghn.token:}")
    private String apiToken;

    @Value("${carrier.ghn.shop-id:}")
    private String shopId;

    @Value("${carrier.ghn.base-url:" + DEFAULT_BASE_URL + "}")
    private String baseUrl;

    @Value("${carrier.ghn.pick-name:}")
    private String pickName;

    @Value("${carrier.ghn.pick-address:}")
    private String pickAddress;

    @Value("${carrier.ghn.pick-ward-code:}")
    private String pickWardCode;

    @Value("${carrier.ghn.pick-district-id:}")
    private Integer pickDistrictId;

    @Value("${carrier.ghn.pick-tel:}")
    private String pickTel;

    @Override
    public String createShipment(Shipment shipment, Order order) {
        validateConfiguration();
        log.info("Creating GHN shipment for order {}", order.getId());

        String url = baseUrl + "/shiip/public-api/v2/shipping-order/create";
        
        GHNCreateOrderRequest request = buildCreateOrderRequest(order);
        
        HttpHeaders headers = createHeaders();
        HttpEntity<GHNCreateOrderRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<GHNCreateOrderResponse> response = restTemplate.postForEntity(
                    url, entity, GHNCreateOrderResponse.class);
            
            GHNCreateOrderResponse body = response.getBody();
            if (body == null || body.getCode() == null || body.getCode() != 200 || body.getData() == null) {
                String errorMsg = body != null ? body.getMessage() : "Unknown error";
                throw new ErrorException(HttpStatus.BAD_GATEWAY, 
                        "Failed to create GHN shipment: " + errorMsg);
            }
            
            String orderCode = body.getData().getOrderCode();
            log.info("GHN shipment created successfully: orderCode={}, orderId={}", orderCode, order.getId());
            
            // Register webhook if needed
            try {
                registerWebhook(orderCode);
            } catch (Exception e) {
                log.warn("Failed to register GHN webhook: {}", e.getMessage());
            }
            
            return orderCode;
        } catch (ErrorException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to create GHN shipment for order {}: {}", order.getId(), ex.getMessage());
            throw new ErrorException(HttpStatus.BAD_GATEWAY, 
                    "GHN shipment creation failed: " + ex.getMessage());
        }
    }

    @Override
    public TrackingResponse getTrackingStatus(String trackingNumber) {
        validateConfiguration();

        String url = baseUrl + "/shiip/public-api/v2/shipping-order/detail";
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        GHNTrackingRequest request = GHNTrackingRequest.builder()
                .orderCode(trackingNumber)
                .build();

        HttpEntity<GHNTrackingRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<GHNTrackingResponse> response = restTemplate.postForEntity(
                    url, entity, GHNTrackingResponse.class);

            GHNTrackingResponse body = response.getBody();
            if (body == null || body.getCode() == null || body.getCode() != 200 || body.getData() == null) {
                throw new ErrorException(HttpStatus.BAD_GATEWAY, "Unable to fetch tracking info from GHN");
            }

            GHNOrderData orderData = body.getData();
            List<TrackingEventData> events = mapTrackingHistory(orderData.getLogs());

            return TrackingResponse.builder()
                    .trackingNumber(orderData.getOrderCode())
                    .status(mapStatus(orderData.getStatus()))
                    .currentLocation(orderData.getCurrentWarehouse())
                    .events(events)
                    .build();
        } catch (ErrorException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to fetch tracking info from GHN for {}: {}", trackingNumber, ex.getMessage());
            throw new ErrorException(HttpStatus.BAD_GATEWAY, "GHN tracking service is unavailable");
        }
    }

    @Override
    public boolean supports(String carrier) {
        return "GHN".equalsIgnoreCase(carrier)
                || "GIAOHANGNHANH".equalsIgnoreCase(carrier);
    }

    private void validateConfiguration() {
        if (apiToken == null || apiToken.isBlank()) {
            throw new ErrorException(HttpStatus.SERVICE_UNAVAILABLE, "GHN token is not configured");
        }
        if (shopId == null || shopId.isBlank()) {
            throw new ErrorException(HttpStatus.SERVICE_UNAVAILABLE, "GHN shop-id is not configured");
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private GHNCreateOrderRequest buildCreateOrderRequest(Order order) {
        var shippingAddress = order.getShippingAddress();
        
        // Determine COD amount
        int codAmount = 0;
        if (order.getPayments() != null && !order.getPayments().isEmpty()) {
            var payment = order.getPayments().iterator().next();
            if (payment.getMethod() != null && 
                payment.getMethod().toString().contains("CASH_ON_DELIVERY")) {
                codAmount = order.getTotalAmount().intValue();
            }
        }

        // Build items
        List<GHNItem> items = order.getOrderDetails().stream()
                .map(detail -> {
                    String productCode = detail.getProductDetail() != null && detail.getProductDetail().getId() != null
                            ? String.valueOf(detail.getProductDetail().getId())
                            : String.valueOf(detail.getId());
                    return GHNItem.builder()
                            .name(buildProductName(detail))
                            .code(productCode)
                            .quantity(detail.getQuantity())
                            .price(detail.getUnitPrice().intValue())
                            .weight(200) // Default 200g per item
                            .build();
                })
                .collect(Collectors.toList());

        // Calculate total weight (grams)
        int totalWeight = items.stream()
                .mapToInt(item -> item.getWeight() * item.getQuantity())
                .sum();

        String customerName = shippingAddress.getFullName() != null
                ? shippingAddress.getFullName()
                : order.getUser().getUsername();

        String customerPhone = shippingAddress.getPhone() != null
                ? shippingAddress.getPhone()
                : pickTel; // fallback to shop phone if missing

        String note = order.getNote() != null && !order.getNote().isBlank()
                ? order.getNote()
                : "Giao giờ hành chính";

        // Determine payment_type_id: 1 = Shop trả phí ship, 2 = Khách trả phí ship
        // Default: 1 (shop trả) - có thể config hoặc dựa vào business logic
        int paymentTypeId = 1;

        // Determine service_type_id: 1 = Express, 2 = Standard, 3 = Economy
        // Default: 2 (Standard delivery) - có thể config
        int serviceTypeId = 2;

        // Get district ID - must not be null
        Integer districtId = getDistrictId(shippingAddress);
        if (districtId == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, 
                    "District ID is required for GHN shipment. Please ensure shipping address has valid districtId field or configure carrier.ghn.pick-district-id as fallback.");
        }

        // Get ward code - must not be null or empty
        String wardCode = getWardCode(shippingAddress);
        if (wardCode == null || wardCode.isBlank()) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, 
                    "Ward code is required for GHN shipment. Please ensure shipping address has valid wardCode field (GHN ward code, not ward name).");
        }

        return GHNCreateOrderRequest.builder()
                .shopId(Integer.parseInt(shopId))
                .clientOrderCode(String.valueOf(order.getId()))
                .toName(customerName)
                .toPhone(customerPhone)
                .toAddress(shippingAddress.getLine())
                .toWardCode(wardCode)
                .toDistrictId(districtId)
                .fromDistrictId(pickDistrictId) // District ID của shop (người gửi)
                .codAmount(codAmount)
                .content(note)
                .requiredNote("KHONGCHOXEMHANG") // Required by GHN
                .paymentTypeId(paymentTypeId) // Required by GHN: 1 = shop pays, 2 = customer pays
                .serviceTypeId(serviceTypeId) // Required by GHN: 1 = Express, 2 = Standard, 3 = Economy
                .weight(totalWeight)
                .length(10) // Default dimensions
                .width(10)
                .height(10)
                .items(items)
                .build();
    }

    private Integer getDistrictId(com.spring.fit.backend.user.domain.entity.AddressEntity address) {
        // Priority 1: Use districtId from address entity (if available)
        if (address.getDistrictId() != null) {
            return address.getDistrictId();
        }
        // Priority 2: Fallback to configured pick-district-id
        return pickDistrictId;
    }

    private String getWardCode(com.spring.fit.backend.user.domain.entity.AddressEntity address) {
        // Priority 1: Use wardCode from address entity (GHN ward code)
        if (address.getWardCode() != null && !address.getWardCode().isBlank()) {
            return address.getWardCode();
        }
        // Priority 2: Fallback to configured pick-ward-code
        if (pickWardCode != null && !pickWardCode.isBlank()) {
            return pickWardCode;
        }
        // If neither available, return null (will throw error)
        return null;
    }

    private String buildProductName(com.spring.fit.backend.order.domain.entity.OrderDetail detail) {
        StringBuilder name = new StringBuilder(detail.getTitle());
        if (detail.getColorLabel() != null && !detail.getColorLabel().isBlank()) {
            name.append(" - ").append(detail.getColorLabel());
        }
        if (detail.getSizeLabel() != null && !detail.getSizeLabel().isBlank()) {
            name.append(" - ").append(detail.getSizeLabel());
        }
        return name.toString();
    }

    private void registerWebhook(String orderCode) {
        // GHN webhook registration (if supported)
        // Implementation depends on GHN API
        log.info("GHN webhook registration for orderCode: {}", orderCode);
    }

    private List<TrackingEventData> mapTrackingHistory(List<GHNLogItem> logs) {
        if (CollectionUtils.isEmpty(logs)) {
            return Collections.emptyList();
        }

        return logs.stream()
                .map(log -> TrackingEventData.builder()
                        .status(mapStatus(log.getStatus()))
                        .location(log.getLocation())
                        .description(log.getDescription())
                        .eventTime(parseDate(log.getTime()))
                        .build())
                .collect(Collectors.toList());
    }

    private LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, GHN_DATE_FORMATTER);
        } catch (Exception ex) {
            log.warn("Unable to parse GHN datetime [{}]: {}", value, ex.getMessage());
            return null;
        }
    }

    private OrderStatus mapStatus(String status) {
        if (status == null || status.isBlank()) {
            return OrderStatus.PENDING;
        }

        // GHN status mapping
        return switch (status.toUpperCase()) {
            case "ready_to_pick", "picking" -> OrderStatus.CONFIRMED;
            case "storing", "transporting", "sorting", "delivering" -> OrderStatus.SHIPPED;
            case "delivered" -> OrderStatus.DELIVERED;
            case "cancel" -> OrderStatus.CANCELLED;
            default -> OrderStatus.PENDING;
        };
    }

    // DTOs for GHN API
    @Data
    @Builder
    private static class GHNCreateOrderRequest {

        @JsonProperty("shop_id")
        private Integer shopId;

        // Mã đơn hàng phía shop để đối chiếu (không bắt buộc nhưng nên gửi)
        @JsonProperty("client_order_code")
        private String clientOrderCode;

        @JsonProperty("to_name")
        private String toName;

        @JsonProperty("to_phone")
        private String toPhone;

        @JsonProperty("to_address")
        private String toAddress;

        @JsonProperty("to_ward_code")
        private String toWardCode;

        @JsonProperty("to_district_id")
        private Integer toDistrictId;

        // District ID của shop (người gửi) - optional nhưng nên gửi để GHN tính phí chính xác
        @JsonProperty("from_district_id")
        private Integer fromDistrictId;

        @JsonProperty("cod_amount")
        private Integer codAmount;

        // Mô tả đơn hàng
        @JsonProperty("content")
        private String content;

        // Bắt buộc theo GHN: CHOXEMHANGKHONGTHU | KHONGCHOXEMHANG
        @JsonProperty("required_note")
        private String requiredNote;

        // Bắt buộc: 1 = Shop trả phí ship (người gửi), 2 = Khách trả phí ship (người nhận)
        @JsonProperty("payment_type_id")
        private Integer paymentTypeId;

        // Bắt buộc: Loại dịch vụ vận chuyển (1 = Express, 2 = Standard, 3 = Economy, ...)
        @JsonProperty("service_type_id")
        private Integer serviceTypeId;

        @JsonProperty("weight")
        private Integer weight;

        @JsonProperty("length")
        private Integer length;

        @JsonProperty("width")
        private Integer width;

        @JsonProperty("height")
        private Integer height;

        @JsonProperty("items")
        private List<GHNItem> items;
    }

    @Data
    @Builder
    private static class GHNItem {

        @JsonProperty("name")
        private String name;

        @JsonProperty("code")
        private String code;

        @JsonProperty("quantity")
        private Integer quantity;

        @JsonProperty("price")
        private Integer price;

        @JsonProperty("weight")
        private Integer weight;
    }

    @Data
    private static class GHNCreateOrderResponse {
        private Integer code;
        private String message;
        private GHNOrderResponseData data;
    }

    @Data
    private static class GHNOrderResponseData {
        @JsonProperty("order_code")
        private String orderCode;
        
        @JsonProperty("sort_code")
        private String sortCode;
        
        @JsonProperty("trans_type")
        private String transType;
        
        @JsonProperty("ward_encode")
        private String wardEncode;
        
        @JsonProperty("district_encode")
        private String districtEncode;
    }

    @Data
    @Builder
    private static class GHNTrackingRequest {
        @JsonProperty("order_code")
        private String orderCode;
    }

    @Data
    private static class GHNTrackingResponse {
        private Integer code;
        private String message;
        private GHNOrderData data;
    }

    @Data
    private static class GHNOrderData {
        private String orderCode;
        private String status;
        private String currentWarehouse;
        private List<GHNLogItem> logs;
    }

    @Data
    private static class GHNLogItem {
        private String status;
        private String location;
        private String description;
        private String time;
    }
}

