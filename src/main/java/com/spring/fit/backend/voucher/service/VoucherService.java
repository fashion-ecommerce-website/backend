package com.spring.fit.backend.voucher.service;

import com.cloudinary.api.exceptions.BadRequest;
import com.spring.fit.backend.voucher.domain.dto.VoucherByUserResponse;
import com.spring.fit.backend.voucher.domain.dto.VoucherValidateRequest;
import com.spring.fit.backend.voucher.domain.dto.VoucherValidateResponse;
import com.spring.fit.backend.voucher.domain.dto.AdminVoucherRequest;
import com.spring.fit.backend.voucher.domain.dto.AdminVoucherResponse;
import com.spring.fit.backend.voucher.domain.entity.Voucher;
import com.spring.fit.backend.common.model.response.PageResult;

public interface VoucherService {
    
    /**
     * Validate voucher code
     * @param request Voucher validate request
     * @param userId User ID từ JWT token
     * @return VoucherValidateResponse
     */
    VoucherValidateResponse validateVoucher(VoucherValidateRequest request, Long userId);
    
    /**
     * Tính toán discount preview
     * @param voucher Voucher entity
     * @param subtotal Giá trị đơn hàng
     * @return Số tiền giảm giá
     */
    java.math.BigDecimal calculateDiscount(Voucher voucher, Double subtotal);

    /**
     * Lấy danh sách voucher kèm khả dụng theo user
     * @param userId ID user
     * @param subtotal Giá trị đơn hàng (tùy chọn để kiểm tra minOrder)
     * @return Danh sách voucher với isAvailable và message
     */
    java.util.List<VoucherByUserResponse> getVouchersByUser(Long userId, Double subtotal);

    // --- Admin CRUD ---
    PageResult<AdminVoucherResponse> searchAdminVouchers(String code, String name, Boolean isActive, int page, int pageSize);

    AdminVoucherResponse createAdminVoucher(AdminVoucherRequest request);

    AdminVoucherResponse updateAdminVoucher(Long id, AdminVoucherRequest request);

    AdminVoucherResponse getAdminVoucher(Long id);

    void toggleAdminVoucherActive(Long id);

    // Apply voucher with locking to enforce usage limits atomically
    void applyVoucher(VoucherValidateRequest request, Long userId, Long orderId) throws BadRequest;

    // Cancel usage (e.g., when order is cancelled)
    void cancelVoucherUsageByOrderId(Long orderId);
}


