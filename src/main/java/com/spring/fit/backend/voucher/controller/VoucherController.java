package com.spring.fit.backend.voucher.controller;

import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.voucher.domain.dto.VoucherByUserResponse;
import com.spring.fit.backend.voucher.domain.dto.VoucherValidateRequest;
import com.spring.fit.backend.voucher.domain.dto.VoucherValidateResponse;
import com.spring.fit.backend.voucher.service.VoucherService;
import com.spring.fit.backend.security.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Slf4j
public class VoucherController {

    private final VoucherService voucherService;
    private final UserRepository userRepository;

    /**
     * Validate voucher code
     * GET /api/vouchers/validate?code=T10SALE&subtotal=600000
     */
    @GetMapping("/validate")
    public ResponseEntity<VoucherValidateResponse> validateVoucher(
            @Valid VoucherValidateRequest request) {
        
        log.info("Validating voucher: {}", request.getCode());
        
        // Get user email from SecurityContext, then resolve userId
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long userId = user.getId();
        
        // Validate voucher
        VoucherValidateResponse response = voucherService.validateVoucher(request, userId);
        
        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách voucher theo user
     * GET /api/vouchers/by-user?subtotal=600000&searchCode=T10SALE
     */
    @GetMapping("/by-user")
    public ResponseEntity<List<VoucherByUserResponse>> getVouchersByUser(
            @RequestParam(required = false) Double subtotal,
            @RequestParam(required = false) String searchCode) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long userId = user.getId();

        List<VoucherByUserResponse> responses = voucherService.getVouchersByUser(userId, subtotal, searchCode);
        return ResponseEntity.ok(responses);
    }

}
