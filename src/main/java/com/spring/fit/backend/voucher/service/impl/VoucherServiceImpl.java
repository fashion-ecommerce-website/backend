package com.spring.fit.backend.voucher.service.impl;

import com.spring.fit.backend.common.enums.AudienceType;
import com.spring.fit.backend.common.enums.VoucherType;
import com.spring.fit.backend.common.enums.VoucherUsageStatus;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.user.domain.entity.UserRank;
import com.spring.fit.backend.user.repository.UserRankRepository;
import com.spring.fit.backend.voucher.domain.dto.VoucherByUserResponse;
import com.spring.fit.backend.voucher.domain.dto.AdminVoucherRequest;
import com.spring.fit.backend.voucher.domain.dto.AdminVoucherResponse;
import com.spring.fit.backend.voucher.domain.dto.VoucherValidateRequest;
import com.spring.fit.backend.voucher.domain.dto.VoucherValidateResponse;
import com.spring.fit.backend.voucher.domain.entity.Voucher;
import com.spring.fit.backend.voucher.domain.entity.VoucherRankRule;
import com.spring.fit.backend.voucher.domain.entity.VoucherUsage;
import com.spring.fit.backend.voucher.repository.VoucherRankRuleRepository;
import com.spring.fit.backend.voucher.repository.VoucherRepository;
import com.spring.fit.backend.voucher.repository.VoucherUsageRepository;
import com.spring.fit.backend.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final VoucherRankRuleRepository voucherRankRuleRepository;
    private final UserRepository userRepository;
    private final UserRankRepository userRankRepository;

    @Override
    public VoucherValidateResponse validateVoucher(VoucherValidateRequest request, Long userId) {
        log.info("Inside VoucherServiceImpl.validateVoucher, validating voucher code: {} for user: {}", request.getCode(), userId);

        // 1. Find voucher by code
        Voucher voucher = voucherRepository.findValidVoucherByCode(
            request.getCode(), 
            LocalDateTime.now(), 
            request.getSubtotal()
        ).orElse(null);

         if (voucher == null) {
             return VoucherValidateResponse.builder()
                 .valid(false)
                 .message("Voucher code not found or invalid")
                 .build();
         }

        // 2. Check if user exists
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Check usage limits
         if (!checkUsageLimits(voucher, userId)) {
             return VoucherValidateResponse.builder()
                 .valid(false)
                 .voucherId(voucher.getId())
                 .code(voucher.getCode())
                 .message("Voucher usage limit exceeded")
                 .build();
        }

        // 4. Check audience type (RANK)
         if (!checkAudienceType(voucher, user)) {
             return VoucherValidateResponse.builder()
                 .valid(false)
                 .voucherId(voucher.getId())
                 .code(voucher.getCode())
                 .message("Voucher not applicable for your rank")
                 .build();
         }

        // 5. Calculate discount preview
        BigDecimal discountPreview = calculateDiscount(voucher, request.getSubtotal());

         return VoucherValidateResponse.builder()
             .valid(true)
             .voucherId(voucher.getId())
             .code(voucher.getCode())
             .type(voucher.getType())
             .value(voucher.getValue())
             .maxDiscount(voucher.getMaxDiscount())
             .discountPreview(discountPreview)
             .message("Voucher is valid")
             .build();
    }

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, Double subtotal) {
        BigDecimal subtotalDecimal = BigDecimal.valueOf(subtotal);
        
        switch (voucher.getType()) {
            case PERCENT:
                BigDecimal percentDiscount = subtotalDecimal
                    .multiply(voucher.getValue())
                    .divide(BigDecimal.valueOf(100));
                
                // Apply max discount if exists
                if (voucher.getMaxDiscount() != null && 
                    percentDiscount.compareTo(voucher.getMaxDiscount()) > 0) {
                    return voucher.getMaxDiscount();
                }
                return percentDiscount;
                
            case FIXED:
                return voucher.getValue();
                
            default:
                return BigDecimal.ZERO;
        }
    }

    @Override
    public List<VoucherByUserResponse> getVouchersByUser(Long userId, Double subtotal, String searchCode) {
        LocalDateTime now = LocalDateTime.now();
        
        List<Voucher> vouchers;
        if (searchCode != null && !searchCode.trim().isEmpty()) {
            vouchers = voucherRepository.findByCodeContainingIgnoreCase(searchCode.trim());
        } else {
            vouchers = voucherRepository.findAll();
        }

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<VoucherByUserResponse> results = new ArrayList<>();
        for (Voucher v : vouchers) {
            boolean available = true;
            String message = null ;

            if (!v.isActive()) continue;

            // Kiểm tra thời gian chỉ khi startAt hoặc endAt có giá trị
            if ((v.getStartAt() != null && v.getStartAt().isAfter(now)) || 
                (v.getEndAt() != null && v.getEndAt().isBefore(now))) {
                available = false;
                message = "Voucher is not active in the current period";
            }

            if (available && v.getMinOrderAmount() != null && subtotal != null &&
                v.getMinOrderAmount().compareTo(BigDecimal.valueOf(subtotal)) > 0) {
                available = false;
                message = "Order subtotal does not meet the minimum requirement";
            }

            // Skip vouchers that exceed usage limits (do not include in list)
            if (available && !checkUsageLimits(v, userId)) {
                continue;
            }

            if (available && !checkAudienceType(v, user)) {
                available = false;
                message = "Voucher not applicable for your rank";
            }

            VoucherByUserResponse dto = VoucherByUserResponse.builder()
                .id(v.getId())
                .name(v.getName())
                .code(v.getCode())
                .type(v.getType())
                .value(v.getValue())
                .maxDiscount(v.getMaxDiscount())
                .minOrderAmount(v.getMinOrderAmount())
                .usageLimitTotal(v.getUsageLimitTotal())
                .usageLimitPerUser(v.getUsageLimitPerUser())
                .startAt(v.getStartAt())
                .endAt(v.getEndAt())
                .isAvailable(available)
                .message(message)
                .build();

            results.add(dto);
        }

        // Sort: available vouchers first
        results.sort(Comparator.comparing(VoucherByUserResponse::isAvailable).reversed());
        return results;
    }

    @Override
    @Transactional
    public void applyVoucher(VoucherValidateRequest request, Long userId, Long orderId) {
        // Lock voucher row to avoid race conditions
        Voucher voucher = voucherRepository.findValidVoucherByCodeForUpdate(
                request.getCode(), LocalDateTime.now(), request.getSubtotal())
                .orElse(null);

        if (voucher == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Voucher not found");
        }

        // Validate usage limits again after locking to prevent race conditions
        if (!checkUsageLimits(voucher, userId)) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Voucher usage limit exceeded");
        }

        // Calculate discount
        BigDecimal discount = calculateDiscount(voucher, request.getSubtotal());

        // Create usage record (APPLIED)
        VoucherUsage usage = VoucherUsage.builder()
                .voucher(voucher)
                .user(UserEntity.builder().id(userId).build())
                .order(orderId == null ? null : Order.builder().id(orderId).build())
                .status(VoucherUsageStatus.APPLIED)
                .discountAmount(discount)
                .usedAt(java.time.LocalDateTime.now())
                .build();
        voucherUsageRepository.save(usage);

    }

    @Override
    @Transactional
    public void cancelVoucherUsageByOrderId(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required to cancel voucher usage");
        }
        voucherUsageRepository.findByOrderId(orderId).ifPresent(usage -> {
            if (usage.getStatus() != VoucherUsageStatus.CANCELLED) {
                usage.setStatus(VoucherUsageStatus.CANCELLED);
                voucherUsageRepository.save(usage);
            }
        });
    }

    // -------------------- Admin CRUD --------------------
    @Override
    public PageResult<AdminVoucherResponse> searchAdminVouchers(String code, String name, Boolean isActive, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        Specification<Voucher> spec = Specification.where(null);
        if (code != null && !code.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
        }
        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }

        Page<Voucher> pageData = voucherRepository.findAll(spec, pageable);
        Page<AdminVoucherResponse> mapped = pageData.map(this::toAdminResponse);
        return PageResult.from(mapped);
    }

    @Override
    @Transactional
    public AdminVoucherResponse createAdminVoucher(AdminVoucherRequest request) {
        // Tự động tạo mã voucher duy nhất
        String code = generateUniqueVoucherCode();
        log.info("Creating voucher with auto-generated code: {}", code);

        validateDates(request);
        validateUsages(request);
        if (request.getType() == VoucherType.PERCENT && request.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("The percentage value cannot exceed 100.");
        }

        Voucher voucher = new Voucher();
        applyRequestToEntityForCreate(request, voucher, code);
        Voucher saved = voucherRepository.save(voucher);
        
        // Tạo rank rules nếu audienceType = RANK
        if (request.getAudienceType() == AudienceType.RANK && request.getRankIds() != null && !request.getRankIds().isEmpty()) {
            createVoucherRankRules(saved, request.getRankIds());
        }
        
        return toAdminResponse(saved);
    }

    @Override
    @Transactional
    public AdminVoucherResponse updateAdminVoucher(Long id, AdminVoucherRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher does not exist"));

        validateDates(request);
        validateUsages(request);
        if (request.getType() == VoucherType.PERCENT && request.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("The percentage value cannot exceed 100.");
        }

        applyRequestToEntityForUpdate(request, voucher);
        Voucher saved = voucherRepository.save(voucher);
        
        // Cập nhật rank rules nếu audienceType = RANK
        if (request.getAudienceType() == AudienceType.RANK) {
            updateVoucherRankRules(saved, request.getRankIds());
        } else {
            // Nếu chuyển từ RANK sang ALL, xóa tất cả rank rules
            voucherRankRuleRepository.deleteByVoucherId(saved.getId());
        }
        
        return toAdminResponse(saved);
    }

    @Override
    public AdminVoucherResponse getAdminVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher does not exist"));
        return toAdminResponse(voucher);
    }

    @Override
    @Transactional
    public void toggleAdminVoucherActive(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher does not exist"));
        voucher.setActive(!voucher.isActive());
        voucherRepository.save(voucher);
    }

    private void validateDates(AdminVoucherRequest request) {
        if (request.getStartAt().isAfter(request.getEndAt())) {
            throw new IllegalArgumentException("startAt must be before or equal to endAt");
        }
    }

    private void validateUsages(AdminVoucherRequest request) {
        if (request.getUsageLimitTotal() < request.getUsageLimitPerUser()) {
            throw new IllegalArgumentException("Usage limit total must be greater or equal to Usage limit per user");
        }
    }

    private void applyRequestToEntityForCreate(AdminVoucherRequest request, Voucher voucher, String code) {
        voucher.setCode(code);
        applyRequestToEntity(request, voucher);
    }

    private void applyRequestToEntityForUpdate(AdminVoucherRequest request, Voucher voucher) {
        applyRequestToEntity(request, voucher);
    }

    private void applyRequestToEntity(AdminVoucherRequest request, Voucher voucher) {
        voucher.setName(request.getName());
        voucher.setType(request.getType());
        voucher.setValue(request.getValue());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setMinOrderAmount(request.getMinOrderAmount());
        voucher.setUsageLimitTotal(request.getUsageLimitTotal());
        voucher.setUsageLimitPerUser(request.getUsageLimitPerUser());
        voucher.setStartAt(request.getStartAt());
        voucher.setEndAt(request.getEndAt());
        voucher.setAudienceType(request.getAudienceType());
        voucher.setActive(Boolean.TRUE.equals(request.getIsActive()));
    }

    private AdminVoucherResponse toAdminResponse(Voucher v) {
        // Lấy danh sách rank IDs nếu audienceType = RANK
        List<Short> rankIds = null;
        if (v.getAudienceType() == AudienceType.RANK) {
            rankIds = voucherRankRuleRepository.findRankIdsByVoucherId(v.getId());
        }
        
        // Đếm số lượt sử dụng voucher (chỉ đếm những lượt có status APPLIED)
        Long usageCount = voucherUsageRepository.countTotalUsage(v.getId(), VoucherUsageStatus.APPLIED);
        
        return AdminVoucherResponse.builder()
                .id(v.getId())
                .name(v.getName())
                .code(v.getCode())
                .type(v.getType())
                .value(v.getValue())
                .maxDiscount(v.getMaxDiscount())
                .minOrderAmount(v.getMinOrderAmount())
                .usageLimitTotal(v.getUsageLimitTotal())
                .usageLimitPerUser(v.getUsageLimitPerUser())
                .startAt(v.getStartAt())
                .endAt(v.getEndAt())
                .isActive(v.isActive())
                .audienceType(v.getAudienceType())
                .rankIds(rankIds)
                .usageCount(usageCount)
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }

    private boolean checkUsageLimits(Voucher voucher, Long userId) {
        // Check usage limit per user
        if (voucher.getUsageLimitPerUser() != null) {
            Long userUsageCount = voucherUsageRepository.countUsageByUser(voucher.getId(), userId, VoucherUsageStatus.APPLIED);
            if (userUsageCount >= voucher.getUsageLimitPerUser()) {
                log.warn("User {} has exceeded usage limit for voucher {}", userId, voucher.getCode());
                return false;
            }
        }

        // Check usage limit total
        if (voucher.getUsageLimitTotal() != null) {
            Long totalUsageCount = voucherUsageRepository.countTotalUsage(voucher.getId(), VoucherUsageStatus.APPLIED);
            if (totalUsageCount >= voucher.getUsageLimitTotal()) {
                log.warn("Voucher {} has exceeded total usage limit", voucher.getCode());
                return false;
            }
        }

        return true;
    }

    private boolean checkAudienceType(Voucher voucher, UserEntity user) {
        if (voucher.getAudienceType() == AudienceType.ALL) {
            return true;
        }

        if (voucher.getAudienceType() == AudienceType.RANK) {
            // Check if user has rank
            if (user.getRankId() == null) {
                log.warn("User {} has no rank assigned", user.getId());
                return false;
            }

            // Check if voucher applies to user rank
            boolean hasRankRule = voucherRankRuleRepository.existsByVoucherIdAndRankId(
                voucher.getId(), 
                user.getRankId()
            );
            
            if (!hasRankRule) {
                log.warn("Voucher {} does not apply to user rank {}", voucher.getCode(), user.getRankId());
                return false;
            }
        }

        return true;
    }

    /**
     * Tạo rank rules cho voucher
     */
    private void createVoucherRankRules(Voucher voucher, List<Short> rankIds) {
        for (Short rankId : rankIds) {
            // Kiểm tra rank có tồn tại không
            if (!userRankRepository.existsById(rankId)) {
                log.warn("Rank ID {} does not exist, skipping", rankId);
                continue;
            }
            
            VoucherRankRule rankRule = VoucherRankRule.builder()
                    .voucher(voucher)
                    .rank(UserRank.builder().id(rankId).build())
                    .build();
            voucherRankRuleRepository.save(rankRule);
        }
        log.info("Created {} rank rules for voucher {}", rankIds.size(), voucher.getCode());
    }

    /**
     * Cập nhật rank rules cho voucher
     */
    private void updateVoucherRankRules(Voucher voucher, List<Short> rankIds) {
        // Lấy danh sách rank IDs hiện tại
        List<Short> existingRankIds = voucherRankRuleRepository.findRankIdsByVoucherId(voucher.getId());
        
        // Xử lý trường hợp rankIds null hoặc empty
        List<Short> newRankIds = (rankIds == null) ? new ArrayList<>() : rankIds;
        
        // Tìm rank IDs cần xóa (có trong existing nhưng không có trong new)
        List<Short> rankIdsToDelete = existingRankIds.stream()
                .filter(existingId -> !newRankIds.contains(existingId))
                .toList();
        
        // Tìm rank IDs cần thêm (có trong new nhưng không có trong existing)
        List<Short> rankIdsToAdd = newRankIds.stream()
                .filter(newId -> !existingRankIds.contains(newId))
                .toList();
        
        // Xóa rank rules không cần thiết
        for (Short rankId : rankIdsToDelete) {
            voucherRankRuleRepository.deleteByVoucherIdAndRankId(voucher.getId(), rankId);
            log.debug("Deleted rank rule for voucher {} and rank {}", voucher.getCode(), rankId);
        }
        
        // Thêm rank rules mới
        for (Short rankId : rankIdsToAdd) {
            // Kiểm tra rank có tồn tại không
            if (!userRankRepository.existsById(rankId)) {
                log.warn("Rank ID {} does not exist, skipping", rankId);
                continue;
            }
            
            VoucherRankRule rankRule = VoucherRankRule.builder()
                    .voucher(voucher)
                    .rank(UserRank.builder().id(rankId).build())
                    .build();
            voucherRankRuleRepository.save(rankRule);
            log.debug("Added rank rule for voucher {} and rank {}", voucher.getCode(), rankId);
        }
        
        log.info("Updated rank rules for voucher {}: deleted {}, added {}", 
                voucher.getCode(), rankIdsToDelete.size(), rankIdsToAdd.size());
    }

    /**
     * Tạo mã voucher ngẫu nhiên 7 ký tự (chữ hoặc số) in hoa
     * @return mã voucher duy nhất
     */
    private String generateUniqueVoucherCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;
        int maxAttempts = 100; // Giới hạn số lần thử để tránh vòng lặp vô hạn
        int attempts = 0;
        
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                int index = random.nextInt(characters.length());
                sb.append(characters.charAt(index));
            }
            code = sb.toString();
            attempts++;
            
            // Kiểm tra tính duy nhất
            if (!voucherRepository.existsByCodeIgnoreCase(code)) {
                log.info("Generated unique voucher code: {}", code);
                return code;
            }
            
            log.debug("Generated duplicate voucher code: {}, attempt: {}", code, attempts);
        } while (attempts < maxAttempts);
        
        // Nếu không tạo được mã duy nhất sau maxAttempts lần thử
        throw new RuntimeException("Unable to generate unique voucher code after " + maxAttempts + " attempts");
    }
}
