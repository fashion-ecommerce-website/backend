package com.spring.fit.backend.order.service.carrier.impl;

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
import org.springframework.http.HttpMethod;
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
public class GHTKCarrierService implements CarrierService {

    private static final String DEFAULT_BASE_URL = "https://services.giaohangtietkiem.vn";
    private static final DateTimeFormatter GHTK_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

    private final RestTemplate restTemplate;

    @Value("${carrier.ghtk.token:}")
    private String apiToken;

    @Value("${carrier.ghtk.base-url:" + DEFAULT_BASE_URL + "}")
    private String baseUrl;

    @Value("${carrier.ghtk.pick-name:}")
    private String pickName;

    @Value("${carrier.ghtk.pick-address:}")
    private String pickAddress;

    @Value("${carrier.ghtk.pick-province:}")
    private String pickProvince;

    @Value("${carrier.ghtk.pick-district:}")
    private String pickDistrict;

    @Value("${carrier.ghtk.pick-ward:}")
    private String pickWard;

    @Value("${carrier.ghtk.pick-tel:}")
    private String pickTel;

    @Override
    public String createShipment(Shipment shipment, Order order) {
        validateConfiguration();
        validatePickAddress();
        log.info("Creating GHTK shipment for order {}", order.getId());

        String url = baseUrl + "/services/shipment/order";
        
        GhtkCreateOrderRequest request = buildCreateOrderRequest(order);
        
        HttpHeaders headers = createHeaders();
        HttpEntity<GhtkCreateOrderRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<GhtkCreateOrderResponse> response = restTemplate.postForEntity(
                    url, entity, GhtkCreateOrderResponse.class);
            
            GhtkCreateOrderResponse body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.getSuccess()) || body.getOrder() == null) {
                String errorMsg = body != null ? body.getMessage() : "Unknown error";
                throw new ErrorException(HttpStatus.BAD_GATEWAY, 
                        "Failed to create GHTK shipment: " + errorMsg);
            }
            
            String labelId = body.getOrder().getLabelId();
            log.info("GHTK shipment created successfully: labelId={}, orderId={}", labelId, order.getId());
            
            return labelId;
        } catch (ErrorException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to create GHTK shipment for order {}: {}", order.getId(), ex.getMessage());
            throw new ErrorException(HttpStatus.BAD_GATEWAY, 
                    "GHTK shipment creation failed: " + ex.getMessage());
        }
    }
    
    private void validatePickAddress() {
        if (pickName == null || pickName.isBlank() ||
            pickAddress == null || pickAddress.isBlank() ||
            pickProvince == null || pickProvince.isBlank() ||
            pickDistrict == null || pickDistrict.isBlank() ||
            pickWard == null || pickWard.isBlank() ||
            pickTel == null || pickTel.isBlank()) {
            throw new ErrorException(HttpStatus.SERVICE_UNAVAILABLE, 
                    "GHTK pick address is not fully configured");
        }
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    
    private GhtkCreateOrderRequest buildCreateOrderRequest(Order order) {
        // Build products list from order details
        List<GhtkProduct> products = order.getOrderDetails().stream()
                .map(detail -> {
                    // Default weight: 0.2kg per item (can be enhanced to get from ProductDetail)
                    double weight = 0.2;
                    return GhtkProduct.builder()
                            .name(buildProductName(detail))
                            .weight(weight)
                            .quantity(detail.getQuantity())
                            .price(detail.getUnitPrice().intValue())
                            .build();
                })
                .collect(Collectors.toList());
        
        // Build order info
        var shippingAddress = order.getShippingAddress();
        
        // Determine COD amount (pick_money)
        // If payment method is COD, use totalAmount, otherwise 0
        int codAmount = 0;
        if (order.getPayments() != null && !order.getPayments().isEmpty()) {
            var payment = order.getPayments().iterator().next();
            if (payment.getMethod() != null && 
                payment.getMethod().toString().contains("CASH_ON_DELIVERY")) {
                codAmount = order.getTotalAmount().intValue();
            }
        }
        
        GhtkOrderInfo orderInfo = GhtkOrderInfo.builder()
                .id(String.valueOf(order.getId()))
                .pickName(pickName)
                .pickAddress(pickAddress)
                .pickProvince(pickProvince)
                .pickDistrict(pickDistrict)
                .pickWard(pickWard)
                .pickTel(pickTel)
                .name(shippingAddress.getFullName())
                .address(shippingAddress.getLine())
                .province(shippingAddress.getCity() != null ? shippingAddress.getCity() : "")
                .district("") // AddressEntity doesn't have district, may need to add
                .ward(shippingAddress.getWard() != null ? shippingAddress.getWard() : "")
                .tel(shippingAddress.getPhone())
                .hamlet("Khác")
                .isFreeship("0")
                .pickDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .pickMoney(codAmount)
                .note(order.getNote() != null && !order.getNote().isBlank() ? order.getNote() : "")
                .value(order.getSubtotalAmount().intValue())
                .transport("road")
                .build();
        
        return GhtkCreateOrderRequest.builder()
                .products(products)
                .order(orderInfo)
                .build();
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

    @Override
    public TrackingResponse getTrackingStatus(String trackingNumber) {
        validateConfiguration();

        String url = baseUrl + "/services/shipment/v2/" + trackingNumber;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<GhtkTrackingResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GhtkTrackingResponse.class);

            GhtkTrackingResponse body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.getSuccess()) || body.getOrder() == null) {
                throw new ErrorException(HttpStatus.BAD_GATEWAY, "Unable to fetch tracking info from GHTK");
            }

            GhtkOrderData orderData = body.getOrder();
            List<TrackingEventData> events = mapTrackingHistory(orderData.getTracking());

            return TrackingResponse.builder()
                    .trackingNumber(orderData.getLabelId())
                    .status(mapStatus(orderData.getStatus()))
                    .currentLocation(orderData.getStatusText())
                    .events(events)
                    .build();
        } catch (ErrorException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to fetch tracking info from GHTK for {}: {}", trackingNumber, ex.getMessage());
            throw new ErrorException(HttpStatus.BAD_GATEWAY, "GHTK tracking service is unavailable");
        }
    }

    @Override
    public boolean supports(String carrier) {
        return "GHTK".equalsIgnoreCase(carrier)
                || "GIAOHANGTIETKIEM".equalsIgnoreCase(carrier);
    }

    private void validateConfiguration() {
        if (apiToken == null || apiToken.isBlank()) {
            throw new ErrorException(HttpStatus.SERVICE_UNAVAILABLE, "GHTK token is not configured");
        }
    }

    private List<TrackingEventData> mapTrackingHistory(List<GhtkTrackingItem> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }

        return items.stream()
                .map(item -> TrackingEventData.builder()
                        .status(mapStatus(item.getStatus()))
                        .location(item.getLocation())
                        .description(item.getStatusText())
                        .eventTime(parseDate(item.getTime()))
                        .build())
                .collect(Collectors.toList());
    }

    private LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, GHTK_DATE_FORMATTER);
        } catch (Exception ex) {
            log.warn("Unable to parse GHTK datetime [{}]: {}", value, ex.getMessage());
            return null;
        }
    }

    private OrderStatus mapStatus(Integer status) {
        if (status == null) {
            return OrderStatus.PENDING;
        }

        return switch (status) {
            case -1, 1, 10 -> OrderStatus.PENDING;
            case 2, 3 -> OrderStatus.PROCESSING;
            case 4, 5, 6, 12 -> OrderStatus.SHIPPED;
            case 7, 8 -> OrderStatus.DELIVERED;
            case 9, 11, 13 -> OrderStatus.CANCELLED;
            default -> OrderStatus.PENDING;
        };
    }

    @Data
    private static class GhtkTrackingResponse {
        private Boolean success;
        private String message;
        private GhtkOrderData order;
    }

    @Data
    private static class GhtkOrderData {
        private String labelId;
        private String partnerId;
        private Integer status;
        private String statusText;
        private String createdDate;
        private String updatedDate;
        private String pickDate;
        private String deliverDate;
        private Integer fee;
        private Integer insuranceFee;
        private Integer pickMoney;
        private List<GhtkTrackingItem> tracking;
    }

    @Data
    private static class GhtkTrackingItem {
        private Integer status;
        private String statusText;
        private String time;
        private String location;
    }
    
    // DTOs for Create Shipment API
    @Data
    @Builder
    private static class GhtkCreateOrderRequest {
        private List<GhtkProduct> products;
        private GhtkOrderInfo order;
    }
    
    @Data
    @Builder
    private static class GhtkProduct {
        private String name;
        private Double weight;
        private Integer quantity;
        private Integer price;
    }
    
    @Data
    @Builder
    private static class GhtkOrderInfo {
        private String id;
        private String pickName;
        private String pickAddress;
        private String pickProvince;
        private String pickDistrict;
        private String pickWard;
        private String pickTel;
        private String name;
        private String address;
        private String province;
        private String district;
        private String ward;
        private String tel;
        private String hamlet;
        private String isFreeship;
        private String pickDate;
        private Integer pickMoney;
        private String note;
        private Integer value;
        private String transport;
    }
    
    @Data
    private static class GhtkCreateOrderResponse {
        private Boolean success;
        private String message;
        private GhtkOrderResponse order;
    }
    
    @Data
    private static class GhtkOrderResponse {
        private String labelId;
        private String partnerId;
        private String status;
        private Integer fee;
        private String estimatedPickTime;
        private String estimatedDeliverTime;
    }
}


