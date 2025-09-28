package com.spring.fit.backend.order.service;

import com.spring.fit.backend.common.enums.FulfillmentStatus;
import com.spring.fit.backend.common.enums.PaymentStatus;
import com.spring.fit.backend.order.domain.dto.request.CreateOrderRequest;
import com.spring.fit.backend.order.domain.dto.request.UpdateOrderRequest;
import com.spring.fit.backend.order.domain.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderService {

    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByIdWithAllRelations(Long id);

    OrderResponse updateOrder(Long id, UpdateOrderRequest request);

    void deleteOrder(Long id);

    Page<OrderResponse> getAllOrders(Pageable pageable);

    Page<OrderResponse> getAllOrdersWithFilters(Long userId, FulfillmentStatus status, PaymentStatus paymentStatus,
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable);

    Page<OrderResponse> getOrdersByStatus(FulfillmentStatus status, Pageable pageable);

    Page<OrderResponse> getOrdersByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    Page<OrderResponse> getOrdersByUserIdAndStatus(Long userId, FulfillmentStatus status, Pageable pageable);

    Page<OrderResponse> getOrdersByUserIdAndPaymentStatus(Long userId, PaymentStatus paymentStatus, Pageable pageable);

    Page<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<OrderResponse> getOrdersByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable);

    OrderResponse updateOrderStatus(Long id, FulfillmentStatus status);

    OrderResponse updatePaymentStatus(Long id, PaymentStatus paymentStatus);

    OrderResponse calculateOrderTotals(Long id);
}
