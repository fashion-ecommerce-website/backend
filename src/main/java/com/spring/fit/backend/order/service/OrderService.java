package com.spring.fit.backend.order.service;

import com.spring.fit.backend.common.enums.FulfillmentStatus;
import com.spring.fit.backend.common.enums.PaymentStatus;
import com.spring.fit.backend.order.domain.dto.request.CreateOrderRequest;
import com.spring.fit.backend.order.domain.dto.request.UpdateOrderRequest;
import com.spring.fit.backend.order.domain.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByIdWithAllRelations(Long id);

    OrderResponse updateOrder(Long id, UpdateOrderRequest request);

    void deleteOrder(Long id);

    Page<OrderResponse> getAllOrders(Pageable pageable);

    Page<OrderResponse> getAllOrdersWithFilters(Long userId, FulfillmentStatus status, PaymentStatus paymentStatus, String sortBy, String direction, Pageable pageable);
    
    Page<OrderResponse> getAllOrdersWithFilters(Long userId, FulfillmentStatus status, PaymentStatus paymentStatus, String keyword, String sortBy, String direction, Pageable pageable);
    
    /**
     * Cancel an order and restore stock
     * Only allows cancelling orders with UNPAID or CANCELLED payment status
     * 
     * @param orderId The order ID to cancel
     * @param userId The user ID (for authorization)
     * @return Updated order response
     */
    OrderResponse cancelOrder(Long orderId, Long userId);
    
}
