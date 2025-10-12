package com.spring.fit.backend.voucher.controller;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.voucher.domain.dto.AdminVoucherRequest;
import com.spring.fit.backend.voucher.domain.dto.AdminVoucherResponse;
import com.spring.fit.backend.voucher.service.VoucherService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<PageResult<AdminVoucherResponse>> search(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(100) int pageSize
    ) {
        PageResult<AdminVoucherResponse> result = voucherService.searchAdminVouchers(code, name, isActive, page, pageSize);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminVoucherResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getAdminVoucher(id));
    }

    @PostMapping
    public ResponseEntity<AdminVoucherResponse> create(@Valid @RequestBody AdminVoucherRequest request) {
        AdminVoucherResponse created = voucherService.createAdminVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminVoucherResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminVoucherRequest request
    ) {
        AdminVoucherResponse updated = voucherService.updateAdminVoucher(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        voucherService.toggleAdminVoucherActive(id);
        return ResponseEntity.noContent().build();
    }
}
