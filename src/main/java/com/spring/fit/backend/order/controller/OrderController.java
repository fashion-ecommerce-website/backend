package com.spring.fit.backend.order.controller;

import com.spring.fit.backend.common.enums.FulfillmentStatus;
import com.spring.fit.backend.common.enums.PaymentStatus;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.domain.dto.request.CreateOrderRequest;
import com.spring.fit.backend.order.domain.dto.request.UpdateOrderRequest;
import com.spring.fit.backend.order.domain.dto.response.OrderResponse;
import com.spring.fit.backend.order.service.OrderService;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    // Create a new order
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
        log.info("Inside OrderController.createOrder create order for userId: {}", user.getId());

        OrderResponse response = orderService.createOrder(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get order by ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        log.info("Inside OrderController.getOrderById order by id: {}", id);

        OrderResponse response = orderService.getOrderById(id);

        return ResponseEntity.ok(response);
    }

    // Get order by ID with all relationships
    @GetMapping("/{id}/full")
    public ResponseEntity<OrderResponse> getOrderByIdWithAllRelations(@PathVariable Long id) {
        log.info("Inside OrderController.getOrderByIdWithAllRelations all relations by id: {}", id);

        OrderResponse response = orderService.getOrderByIdWithAllRelations(id);

        return ResponseEntity.ok(response);
    }

    // Update order
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {

        log.info("Inside OrderController.updateOrder Updating order with id: {}", id);

        OrderResponse response = orderService.updateOrder(id, request);

        return ResponseEntity.ok(response);
    }

    // Delete order
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        log.info("Inside OrderController.deleteOrder Deleting order with id: {}", id);

        orderService.deleteOrder(id);

        return ResponseEntity.ok().build();
    }

    // Get all orders with optional filters and pagination
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) FulfillmentStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info(
                "Inside OrderController.getAllOrders getting all orders with filters - userId: {}, status: {}, paymentStatus: {}, startDate: {}, endDate: {}",
                userId, status, paymentStatus, startDate, endDate);

        Page<OrderResponse> response = orderService.getAllOrdersWithFilters(userId, status, paymentStatus, startDate,
                endDate, pageable);

        return ResponseEntity.ok(response);
    }

}