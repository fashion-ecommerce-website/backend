package com.spring.fit.backend.order.service.impl;

import com.spring.fit.backend.common.enums.FulfillmentStatus;
import com.spring.fit.backend.common.enums.PaymentStatus;
import com.spring.fit.backend.common.exception.ErrorException;
import org.springframework.http.HttpStatus;
import com.spring.fit.backend.order.domain.dto.request.CreateOrderRequest;
import com.spring.fit.backend.order.domain.dto.request.UpdateOrderRequest;
import com.spring.fit.backend.order.domain.dto.response.OrderResponse;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.domain.entity.OrderDetail;
import com.spring.fit.backend.order.repository.OrderRepository;
import com.spring.fit.backend.order.service.OrderService;
import com.spring.fit.backend.payment.domain.entity.Payment;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionApplyRequest;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionApplyResponse;
import com.spring.fit.backend.promotion.service.PromotionService;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.user.repository.AddressRepository;
import com.spring.fit.backend.voucher.domain.entity.Voucher;
import com.spring.fit.backend.voucher.repository.VoucherRepository;
import com.spring.fit.backend.common.util.PagingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductDetailRepository productDetailRepository;
    private final VoucherRepository voucherRepository;
    private final PromotionService promotionService;

    @Override
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {

        if (userId == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Inside OrderServiceImpl.createOrder, user ID is required for order creation");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Inside OrderServiceImpl.createOrder, user not found with id: " + userId));

        var shippingAddress = addressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND,
                        "Inside OrderServiceImpl.createOrder, shipping address not found with id: " + request.getShippingAddressId()));

        if (!shippingAddress.getUser().getId().equals(userId)) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Inside OrderServiceImpl.createOrder, shipping address does not belong to user");
        }

        // Handle voucher if provided - validate and set voucher relationship
        Voucher voucher = null;
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            voucher = voucherRepository.findValidVoucherByCode(
                    request.getVoucherCode(), 
                    LocalDateTime.now(), 
                    request.getSubtotalAmount().doubleValue()
            ).orElseThrow(() -> new ErrorException(HttpStatus.BAD_REQUEST, 
                    "Inside OrderServiceImpl.createOrder, invalid or expired voucher code: {}" + request.getVoucherCode()));
        }

        // Create order
        var order = Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .note(request.getNote())
                .subtotalAmount(BigDecimal.valueOf(request.getSubtotalAmount()))
                .discountAmount(BigDecimal.valueOf(request.getDiscountAmount()))
                .shippingFee(BigDecimal.valueOf(request.getShippingFee()))
                .totalAmount(BigDecimal.valueOf(request.getTotalAmount()))
                .status(FulfillmentStatus.UNFULFILLED)
                .paymentStatus(PaymentStatus.UNPAID)
                .voucher(voucher != null ? voucher : null)
                .build();

        // Create order details
        for (var detailRequest : request.getOrderDetails()) {
            var productDetail = productDetailRepository.findById(detailRequest.getProductDetailId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND,
                            "Inside OrderServiceImpl.createOrder, product detail not found with id: " + detailRequest.getProductDetailId()));

            // Get promotion ID if provided
            Long promotionId = detailRequest.getPromotionId();

            var orderDetail = OrderDetail.builder()
                    .order(order)
                    .productDetail(productDetail)
                    .title(productDetail.getProduct().getTitle())
                    .colorLabel(productDetail.getColor().getName())
                    .sizeLabel(productDetail.getSize().getLabel())
                    .quantity(detailRequest.getQuantity())
                    .unitPrice(productDetail.getPrice())
                    .promotionId(promotionId)
                    .build();

            order.getOrderDetails().add(orderDetail);
        }

        // Create payment
        var payment = Payment.builder()
                .order(order)
                .method(request.getPaymentMethod())
                .status(PaymentStatus.UNPAID)
                .amount(order.getTotalAmount())
                .build();

        order.getPayments().add(payment);

        // Save order
        var savedOrder = orderRepository.save(order);
        log.info("Inside OrderServiceImpl.createOrder created successfully with id: {}", savedOrder.getId());

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Inside OrderServiceImpl.getOrderById, getting order by id: {}", id);
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Inside OrderServiceImpl.getOrderById, order not found with id: " + id));
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdWithAllRelations(Long id) {
        log.info("Inside OrderServiceImpl.getOrderByIdWithAllRelations, getting order with all relations by id: {}", id);
        var order = orderRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Inside OrderServiceImpl.getOrderByIdWithAllRelations, order not found with id: " + id));
        return mapToResponse(order);
    }

    @Override
    public OrderResponse updateOrder(Long id, UpdateOrderRequest request) {
        log.info("Inside OrderServiceImpl.updateOrder, updating order with id: {}", id);
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Inside OrderServiceImpl.updateOrder, order not found with id: " + id));

        // Update fields if provided
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }
        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
        }
        if (request.getCurrency() != null) {
            order.setCurrency(request.getCurrency());
        }
        if (request.getSubtotalAmount() != null) {
            order.setSubtotalAmount(request.getSubtotalAmount());
        }
        if (request.getDiscountAmount() != null) {
            order.setDiscountAmount(request.getDiscountAmount());
        }
        if (request.getShippingFee() != null) {
            order.setShippingFee(request.getShippingFee());
        }
        if (request.getTotalAmount() != null) {
            order.setTotalAmount(request.getTotalAmount());
        }
        if (request.getNote() != null) {
            order.setNote(request.getNote());
        }

        var savedOrder = orderRepository.save(order);
        log.info("Inside OrderServiceImpl.updateOrder, order updated successfully with id: {}", savedOrder.getId());

        return mapToResponse(savedOrder);
    }

    @Override
    public void deleteOrder(Long id) {
        log.info("Deleting order with id: {}", id);
        if (!orderRepository.existsById(id)) {
            throw new ErrorException(HttpStatus.NOT_FOUND, "Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
        log.info("Order deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.info("Getting all orders with pagination");
        return orderRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrdersWithFilters(Long userId, FulfillmentStatus status, PaymentStatus paymentStatus, String sortBy, String direction, Pageable pageable) {
        log.info(
                "Inside OrderServiceImpl.getAllOrdersWithFilters, getting all orders with filters - userId: {}, status: {}, paymentStatus: {}, sortBy: {}, direction: {}",
                userId, status, paymentStatus, sortBy, direction);

        // Build Pageable with sorting using utility
        Pageable sortedPageable = PagingUtils.buildPageableWithSorting(pageable, sortBy, direction);

        return orderRepository.findAllWithFilters(userId, status, paymentStatus, sortedPageable)
                .map(this::mapToResponse);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .userUsername(order.getUser().getUsername())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .currency(order.getCurrency())
                .subtotalAmount(order.getSubtotalAmount())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .note(order.getNote())
                .shippingAddress(mapAddressToResponse(order.getShippingAddress()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderDetails(mapOrderDetailsToResponse(order.getOrderDetails()))
                .payments(mapPaymentsToResponse(order.getPayments()))
                .shipments(mapShipmentsToResponse(order.getShipments()))
                .voucherCode(order.getVoucher() != null ? order.getVoucher().getCode() : null)
                .build();
    }

    private OrderResponse.AddressResponse mapAddressToResponse(
            com.spring.fit.backend.user.domain.entity.AddressEntity address) {
        return OrderResponse.AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .line(address.getLine())
                .ward(address.getWard())
                .city(address.getCity())
                .countryCode(address.getCountryCode())
                .build();
    }

    private List<OrderResponse.OrderDetailResponse> mapOrderDetailsToResponse(Set<OrderDetail> orderDetails) {
        return orderDetails.stream()
                .map(detail -> {
                    // Tạo base response từ entity
                    OrderResponse.OrderDetailResponse baseResponse = OrderResponse.OrderDetailResponse.builder()
                            .id(detail.getId())
                            .productDetailId(detail.getProductDetail().getId())
                            .title(detail.getTitle())
                            .colorLabel(detail.getColorLabel())
                            .sizeLabel(detail.getSizeLabel())
                            .quantity(detail.getQuantity())
                            .unitPrice(detail.getUnitPrice())
                            .totalPrice(detail.getTotalPrice())
                            .build();
                    
                    // Áp dụng promotion
                    var applyRes = PromotionApplyResponse.builder().build();
                    try {
                        var applyReq = PromotionApplyRequest.builder()
                                .skuId(detail.getProductDetail().getId())
                                .basePrice(detail.getUnitPrice())
                                .build();
                        applyRes = promotionService.applyBestPromotionForSku(applyReq);
                    } catch (Exception ex) {
                        // fallback giữ nguyên giá nếu có lỗi
                        applyRes = PromotionApplyResponse.builder()
                                .basePrice(detail.getUnitPrice())
                                .finalPrice(detail.getUnitPrice())
                                .percentOff(0)
                                .build();
                    }
                    
                    // Cập nhật thông tin promotion
                    return OrderResponse.OrderDetailResponse.builder()
                            .id(baseResponse.getId())
                            .productDetailId(baseResponse.getProductDetailId())
                            .title(baseResponse.getTitle())
                            .colorLabel(baseResponse.getColorLabel())
                            .sizeLabel(baseResponse.getSizeLabel())
                            .quantity(baseResponse.getQuantity())
                            .unitPrice(baseResponse.getUnitPrice())
                            .finalPrice(applyRes.getFinalPrice())
                            .percentOff(applyRes.getPercentOff())
                            .promotionId(applyRes.getPromotionId())
                            .promotionName(applyRes.getPromotionName())
                            .totalPrice(baseResponse.getTotalPrice())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<OrderResponse.PaymentResponse> mapPaymentsToResponse(Set<Payment> payments) {
        return payments.stream()
                .map(payment -> OrderResponse.PaymentResponse.builder()
                        .id(payment.getId())
                        .method(payment.getMethod().name())
                        .status(payment.getStatus().name())
                        .amount(payment.getAmount())
                        .provider(payment.getProvider())
                        .transactionNo(payment.getTransactionNo())
                        .paidAt(payment.getPaidAt())
                        .createdAt(payment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private List<OrderResponse.ShipmentResponse> mapShipmentsToResponse(
            Set<com.spring.fit.backend.order.domain.entity.Shipment> shipments) {
        return shipments.stream()
                .map(shipment -> OrderResponse.ShipmentResponse.builder()
                        .id(shipment.getId())
                        .carrier(shipment.getCarrier())
                        .trackingNo(shipment.getTrackingNo())
                        .status(shipment.getStatus() != null ? shipment.getStatus().name() : null)
                        .shippedAt(shipment.getShippedAt())
                        .deliveredAt(shipment.getDeliveredAt())
                        .createdAt(shipment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }


}
