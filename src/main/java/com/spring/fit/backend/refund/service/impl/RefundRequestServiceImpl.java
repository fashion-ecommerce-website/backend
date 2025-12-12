package com.spring.fit.backend.refund.service.impl;

import com.spring.fit.backend.common.enums.PaymentStatus;
import com.spring.fit.backend.common.enums.RefundStatus;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.repository.OrderRepository;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos;
import com.spring.fit.backend.payment.domain.dto.PaymentDtos.RefundResponse;
import com.spring.fit.backend.payment.domain.entity.Payment;
import com.spring.fit.backend.payment.repository.PaymentRepository;
import com.spring.fit.backend.payment.service.PaymentService;
import com.spring.fit.backend.refund.domain.dto.RefundDtos.CreateRefundRequest;
import com.spring.fit.backend.refund.domain.dto.RefundDtos.RefundRequestResponse;
import com.spring.fit.backend.refund.domain.dto.RefundDtos.UpdateRefundStatusRequest;
import com.spring.fit.backend.refund.domain.entity.RefundRequest;
import com.spring.fit.backend.refund.repository.RefundRequestRepository;
import com.spring.fit.backend.refund.service.RefundRequestService;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefundRequestServiceImpl implements RefundRequestService {

    private final RefundRequestRepository refundRequestRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Override
    public RefundRequestResponse createRefundRequest(Long userId, CreateRefundRequest request) {
        log.info("Creating refund request for user {} and order {}", userId, request.getOrderId());

        // Validate user
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));

        // Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Order not found with id: " + request.getOrderId()));

        // Validate order belongs to user
        if (!order.getUser().getId().equals(userId)) {
            throw new ErrorException(HttpStatus.FORBIDDEN, "Order does not belong to user");
        }

        // Validate order payment status
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, 
                "Cannot request refund for order with payment status: " + order.getPaymentStatus());
        }

        // Check if there's already a pending refund request for this order
        refundRequestRepository.findByOrderIdAndStatus(request.getOrderId(), RefundStatus.PENDING)
                .ifPresent(existing -> {
                    throw new ErrorException(HttpStatus.BAD_REQUEST, 
                        "There is already a pending refund request for this order");
                });

        // Determine refund amount
        BigDecimal refundAmount = request.getRefundAmount();
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            refundAmount = order.getTotalAmount();
        }

        // Validate refund amount doesn't exceed order total
        if (refundAmount.compareTo(order.getTotalAmount()) > 0) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, 
                "Refund amount cannot exceed order total amount");
        }

        // Create refund request
        RefundRequest refundRequest = RefundRequest.builder()
                .order(order)
                .user(user)
                .status(RefundStatus.PENDING)
                .reason(request.getReason())
                .refundAmount(refundAmount)
                .build();

        RefundRequest saved = refundRequestRepository.save(refundRequest);
        log.info("Created refund request with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundRequestResponse getRefundRequestById(Long id) {
        log.info("Getting refund request by id: {}", id);
        RefundRequest refundRequest = refundRequestRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Refund request not found with id: " + id));
        return mapToResponse(refundRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RefundRequestResponse> getUserRefundRequests(Long userId, RefundStatus status, Pageable pageable) {
        log.info("Getting refund requests for user {} with status {}", userId, status);
        
        Page<RefundRequest> refundRequests;
        if (status != null) {
            refundRequests = refundRequestRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            refundRequests = refundRequestRepository.findByUserId(userId, pageable);
        }

        return refundRequests.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RefundRequestResponse> getAllRefundRequests(RefundStatus status, Pageable pageable) {
        log.info("Getting all refund requests with status {}", status);
        
        Page<RefundRequest> refundRequests;
        if (status != null) {
            refundRequests = refundRequestRepository.findByStatus(status, pageable);
        } else {
            refundRequests = refundRequestRepository.findAll(pageable);
        }

        return refundRequests.map(this::mapToResponse);
    }

    @Override
    public RefundRequestResponse updateRefundStatus(Long refundRequestId, Long adminUserId, UpdateRefundStatusRequest request) {
        log.info("Updating refund request {} status to {} by admin {}", 
            refundRequestId, request.getStatus(), adminUserId);

        // Validate admin user
        UserEntity adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Admin user not found with id: " + adminUserId));

        // Get refund request
        RefundRequest refundRequest = refundRequestRepository.findByIdWithRelations(refundRequestId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Refund request not found with id: " + refundRequestId));

        // Validate current status
        if (refundRequest.getStatus() != RefundStatus.PENDING) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, 
                "Cannot update refund request that is not in PENDING status. Current status: " + refundRequest.getStatus());
        }

        // Validate new status
        RefundStatus newStatus = request.getStatus();
        if (newStatus != RefundStatus.APPROVED && newStatus != RefundStatus.REJECTED) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, 
                "Can only approve or reject refund requests. Status must be APPROVED or REJECTED");
        }

        // Update status
        refundRequest.setStatus(newStatus);
        refundRequest.setProcessedBy(adminUser);
        refundRequest.setProcessedAt(LocalDateTime.now());
        refundRequest.setAdminNote(request.getAdminNote());

        // If approved, process the refund payment
        if (newStatus == RefundStatus.APPROVED) {
            try {
                // Get the payment for this order
                Payment payment = paymentRepository.findFirstByOrder_IdOrderByIdDesc(refundRequest.getOrder().getId())
                        .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, 
                            "Payment not found for order id: " + refundRequest.getOrder().getId()));

                // Create payment refund request
                PaymentDtos.RefundRequest paymentRefundRequest = PaymentDtos.RefundRequest.builder()
                        .paymentId(payment.getId())
                        .amount(refundRequest.getRefundAmount())
                        .reason(refundRequest.getReason())
                        .build();

                // Process refund through payment service
                RefundResponse refundResponse = paymentService.refundPayment(paymentRefundRequest);

                // Update refund request with stripe refund id
                refundRequest.setStripeRefundId(refundResponse.getRefundId());
                
                // Mark as completed
                refundRequest.setStatus(RefundStatus.COMPLETED);

                log.info("Successfully processed refund for refund request {} with Stripe refund id: {}", 
                    refundRequestId, refundResponse.getRefundId());
            } catch (Exception e) {
                log.error("Failed to process refund for refund request {}: {}", refundRequestId, e.getMessage(), e);
                // Keep status as APPROVED but don't mark as COMPLETED
                // Admin can retry later if needed
                throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Failed to process refund payment: " + e.getMessage());
            }
        }

        RefundRequest saved = refundRequestRepository.save(refundRequest);
        log.info("Updated refund request {} status to {}", refundRequestId, saved.getStatus());

        return mapToResponse(saved);
    }

    private RefundRequestResponse mapToResponse(RefundRequest refundRequest) {
        return RefundRequestResponse.builder()
                .id(refundRequest.getId())
                .orderId(refundRequest.getOrder().getId())
                .userId(refundRequest.getUser().getId())
                .userEmail(refundRequest.getUser().getEmail())
                .status(refundRequest.getStatus())
                .reason(refundRequest.getReason())
                .refundAmount(refundRequest.getRefundAmount())
                .adminNote(refundRequest.getAdminNote())
                .processedBy(refundRequest.getProcessedBy() != null ? refundRequest.getProcessedBy().getId() : null)
                .processedAt(refundRequest.getProcessedAt())
                .stripeRefundId(refundRequest.getStripeRefundId())
                .createdAt(refundRequest.getCreatedAt())
                .updatedAt(refundRequest.getUpdatedAt())
                .build();
    }
}

