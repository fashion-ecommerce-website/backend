package com.spring.fit.backend.refund.controller;

import com.spring.fit.backend.common.enums.RefundStatus;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.refund.domain.dto.RefundDtos.CreateRefundRequest;
import com.spring.fit.backend.refund.domain.dto.RefundDtos.RefundRequestResponse;
import com.spring.fit.backend.refund.domain.dto.RefundDtos.UpdateRefundStatusRequest;
import com.spring.fit.backend.refund.service.RefundRequestService;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Slf4j
public class RefundRequestController {

    private final RefundRequestService refundRequestService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<RefundRequestResponse> createRefundRequest(
            @Valid @RequestBody CreateRefundRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        log.info("Inside RefundRequestController.createRefundRequest creating refund request for user {}", user.getId());

        RefundRequestResponse response = refundRequestService.createRefundRequest(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundRequestResponse> getRefundRequestById(@PathVariable Long id) {
        log.info("Inside RefundRequestController.getRefundRequestById getting refund request {}", id);

        RefundRequestResponse response = refundRequestService.getRefundRequestById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/current-user")
    public ResponseEntity<Page<RefundRequestResponse>> getMyRefundRequests(
            @RequestParam(required = false) RefundStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        log.info("Inside RefundRequestController.getMyRefundRequests getting refund requests for user {}", user.getId());

        Page<RefundRequestResponse> response = refundRequestService.getUserRefundRequests(user.getId(), status, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<RefundRequestResponse>> getAllRefundRequests(
            @RequestParam(required = false) RefundStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Inside AdminRefundRequestController.getAllRefundRequests getting all refund requests with status {}", status);

        Page<RefundRequestResponse> response = refundRequestService.getAllRefundRequests(status, pageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RefundRequestResponse> updateRefundStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRefundStatusRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity adminUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Admin user not found"));

        log.info("Inside AdminRefundRequestController.updateRefundStatus updating refund request {} status to {} by admin {}", 
            id, request.getStatus(), adminUser.getId());

        RefundRequestResponse response = refundRequestService.updateRefundStatus(id, adminUser.getId(), request);

        return ResponseEntity.ok(response);
    }
}



