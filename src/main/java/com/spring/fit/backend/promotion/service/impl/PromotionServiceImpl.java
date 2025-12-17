package com.spring.fit.backend.promotion.service.impl;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.category.repository.CategoryRepository;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import com.spring.fit.backend.product.repository.ProductMainRepository;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionRequest;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionTargetsRemoveRequest;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionTargetsUpsertRequest;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionApplyRequest;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionResponse;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionTargetResponse;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionApplyResponse;
import com.spring.fit.backend.promotion.domain.dto.response.TargetsUpsertResult;
import com.spring.fit.backend.promotion.domain.entity.Promotion;
import com.spring.fit.backend.promotion.domain.entity.PromotionTarget;
import com.spring.fit.backend.promotion.domain.entity.PromotionTargetId;
import com.spring.fit.backend.common.enums.PromotionTargetType;
import com.spring.fit.backend.common.enums.PromotionType;
import com.spring.fit.backend.common.exception.ErrorException;
import org.springframework.http.HttpStatus;
import com.spring.fit.backend.promotion.repository.PromotionRepository;
import com.spring.fit.backend.promotion.repository.PromotionTargetRepository;
import com.spring.fit.backend.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionTargetRepository promotionTargetRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductMainRepository productMainRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        validatePromotionRequestForCreate(request);

        if (request.getTargets() != null && !request.getTargets().isEmpty()) {
            for (PromotionRequest.TargetItem target : request.getTargets()) {
                validateTargetItem(target);
                
                // Kiểm tra tất cả SKUs của target có promotion nào trong period không
                validateNoSkuConflict(target, request.getStartAt(), request.getEndAt(), null);
            }
        }

        Promotion p = new Promotion();
        applyRequestToEntity(request, p);
        Promotion saved = promotionRepository.save(p);

        if (request.getTargets() != null && !request.getTargets().isEmpty()) {
            createTargetsForPromotion(saved, request.getTargets());
        }

        // Reload entity để có targets mới trong response
        Promotion reloaded = promotionRepository.findById(saved.getId()).orElse(saved);
        return toResponse(reloaded);
    }

    @Override
    public PromotionResponse getById(Long id) {
        Promotion p = promotionRepository.findById(id).orElseThrow(() -> new RuntimeException("Promotion not found"));
        return toResponse(p);
    }

    @Override
    public PageResult<PromotionResponse> search(String name, Boolean isActive, LocalDateTime from, LocalDateTime to, String type, int page, int pageSize, String sort) {
        Pageable pageable = buildPageable(sort, page, pageSize);

        Specification<Promotion> spec = Specification.where(null);
        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }
        if (from != null && to != null) {
            spec = spec.and((root, query, cb) -> cb.and(
                    cb.lessThanOrEqualTo(root.get("startAt"), to),
                    cb.greaterThanOrEqualTo(root.get("endAt"), from)
            ));
        }
        if (type != null && !type.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), PromotionType.valueOf(type)));
        }

        Page<Promotion> pageData = promotionRepository.findAll(spec, pageable);
        Page<PromotionResponse> mapped = pageData.map(this::toResponse);
        return PageResult.from(mapped);
    }

    @Override
    @Transactional
    public PromotionResponse update(Long id, PromotionRequest request) {
        validatePromotionRequest(request);
        Promotion p = promotionRepository.findById(id).orElseThrow(() -> new RuntimeException("Promotion not found"));

        validatePromotionNotStarted(p);

        // Nếu request có targets mới, validate và cập nhật targets
        if (request.getTargets() != null) {
            // Validate targets mới
            for (PromotionRequest.TargetItem target : request.getTargets()) {
                validateTargetItem(target);
                // Kiểm tra conflict với promotion khác (loại trừ chính nó)
                validateNoSkuConflict(target, request.getStartAt(), request.getEndAt(), id);
            }

            // Xóa tất cả targets cũ
            promotionTargetRepository.deleteByPromotion_Id(id);

            // Thêm targets mới
            applyRequestToEntity(request, p);
            Promotion saved = promotionRepository.save(p);

            if (!request.getTargets().isEmpty()) {
                createTargetsForPromotion(saved, request.getTargets());
            }

            // Reload entity để có targets mới trong response
            Promotion reloaded = promotionRepository.findById(saved.getId()).orElse(saved);
            return toResponse(reloaded);
        } else {
            // Nếu không có targets trong request, chỉ kiểm tra targets hiện tại với thời gian mới
            for (PromotionTarget existingTarget : p.getPromotionTargets()) {
                PromotionRequest.TargetItem targetItem = PromotionRequest.TargetItem.builder()
                        .targetType(existingTarget.getTargetType())
                        .targetId(existingTarget.getTargetId())
                        .build();
                validateNoSkuConflict(targetItem, request.getStartAt(), request.getEndAt(), id);
            }

            applyRequestToEntity(request, p);
            Promotion saved = promotionRepository.save(p);
            return toResponse(saved);
        }
    }

    @Override
    @Transactional
    public void toggleActive(Long id) {
        Promotion p = promotionRepository.findById(id).orElseThrow(() -> new RuntimeException("Promotion not found"));
        
        // Nếu đang inactive và muốn chuyển sang active, kiểm tra conflict
        if (!p.isActive()) {
            // Kiểm tra tất cả targets của promotion này có conflict với promotion khác không
            for (PromotionTarget existingTarget : p.getPromotionTargets()) {
                PromotionRequest.TargetItem targetItem = PromotionRequest.TargetItem.builder()
                        .targetType(existingTarget.getTargetType())
                        .targetId(existingTarget.getTargetId())
                        .build();
                // Kiểm tra conflict (loại trừ chính promotion này)
                validateNoSkuConflictForActivation(targetItem, p.getStartAt(), p.getEndAt(), id);
            }
        }
        
        p.setActive(!p.isActive());
        promotionRepository.save(p);
    }
    
    /**
     * Kiểm tra conflict khi active promotion
     */
    private void validateNoSkuConflictForActivation(PromotionRequest.TargetItem target,
                                                     LocalDateTime startAt,
                                                     LocalDateTime endAt,
                                                     Long excludePromotionId) {
        List<Long> skuIds = getSkuIdsFromTarget(target);
        
        for (Long skuId : skuIds) {
            Long conflictingPromotionId = promotionTargetRepository.findConflictingPromotionIdForSkuExcluding(
                    skuId, excludePromotionId, startAt, endAt);
            
            if (conflictingPromotionId != null) {
                throw new IllegalArgumentException(
                        "Cannot activate promotion: SKU ID " + skuId + 
                        " already has an active promotion (Promotion ID: " + conflictingPromotionId + 
                        ") in the time period " + startAt + " to " + endAt + 
                        ". Please deactivate the conflicting promotion first.");
            }
        }
    }

    @Override
    @Transactional
    public TargetsUpsertResult upsertTargets(Long promotionId, PromotionTargetsUpsertRequest request) {
        Promotion promotion = promotionRepository.findById(promotionId).orElseThrow(() -> new RuntimeException("Promotion not found"));

        long inserted = 0;
        long ignored = 0;
        Set<PromotionTargetId> seen = new HashSet<>();

        for (PromotionTargetsUpsertRequest.Item item : request.getItems()) {
            validateTargetExists(item);
            PromotionTargetId id = new PromotionTargetId(promotionId, item.getTargetType(), item.getTargetId());
            if (seen.contains(id)) {
                ignored++;
                continue;
            }
            seen.add(id);

            if (promotionTargetRepository.existsById(id)) {
                ignored++;
                continue;
            }
            
            // Kiểm tra tất cả SKUs của target có promotion nào trong period không
            PromotionRequest.TargetItem targetItem = PromotionRequest.TargetItem.builder()
                    .targetType(item.getTargetType())
                    .targetId(item.getTargetId())
                    .build();
            validateNoSkuConflict(targetItem, promotion.getStartAt(), promotion.getEndAt(), promotionId);
            
            PromotionTarget t = PromotionTarget.builder()
                    .id(id)
                    .promotion(promotion)
                    .targetType(item.getTargetType())
                    .targetId(item.getTargetId())
                    .build();
            promotionTargetRepository.save(t);
            inserted++;
        }

        return TargetsUpsertResult.builder().inserted(inserted).ignored(ignored).build();
    }

    @Override
    public PageResult<PromotionTargetResponse> listTargets(Long promotionId, PromotionTargetType type, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<PromotionTarget> pageData = (type == null)
                ? promotionTargetRepository.findByPromotionId(promotionId, pageable)
                : promotionTargetRepository.findByPromotionIdAndTargetType(promotionId, type, pageable);

        Page<com.spring.fit.backend.promotion.domain.dto.response.PromotionTargetResponse> mapped = pageData.map(t ->
                com.spring.fit.backend.promotion.domain.dto.response.PromotionTargetResponse.builder()
                        .promotionId(t.getId().getPromotionId())
                        .targetType(t.getId().getTargetType())
                        .targetId(t.getId().getTargetId())
                        .build()
        );
        return PageResult.from(mapped);
    }

    @Override
    @Transactional
    public long removeTargets(Long promotionId, PromotionTargetsRemoveRequest request) {
        long removed = 0;
        for (PromotionTargetsRemoveRequest.Item item : request.getItems()) {
            PromotionTargetId id = new PromotionTargetId(promotionId, item.getTargetType(), item.getTargetId());
            if (promotionTargetRepository.existsById(id)) {
                promotionTargetRepository.deleteById(id);
                removed++;
            }
        }
        return removed;
    }

    @Override
    public PromotionApplyResponse applyPromotionForSku(PromotionApplyRequest request) {
        var at = request.getAt() == null ? LocalDateTime.now() : request.getAt();
        var sku = productDetailRepository.findById(request.getSkuId())
                .orElseThrow(() -> new RuntimeException("SKU (product_detail) not found"));

        var basePrice = request.getBasePrice() != null ? request.getBasePrice() : sku.getPrice();

        Long productId = sku.getProduct().getId();
        List<Long> categoryIds = new ArrayList<>();
        if (sku.getProduct().getCategories() != null) {
            for (var c : sku.getProduct().getCategories()) {
                categoryIds.add(c.getId().longValue());
            }
        }

        if (categoryIds.isEmpty()) {
            categoryIds.add(-1L);
        }

        Long promotionId = promotionTargetRepository.findPromotionIdForSkuAt(
                sku.getId(), productId, categoryIds, at);

        if (promotionId == null) {
            return PromotionApplyResponse.builder()
                    .basePrice(basePrice)
                    .finalPrice(basePrice)
                    .percentOff(0)
                    .promotionId(null)
                    .promotionName(null)
                    .build();
        }

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElse(null);
        
        if (promotion == null) {
            return PromotionApplyResponse.builder()
                    .basePrice(basePrice)
                    .finalPrice(basePrice)
                    .percentOff(0)
                    .promotionId(null)
                    .promotionName(null)
                    .build();
        }

        // Tính discount
        BigDecimal discount;
        if (promotion.getType() == PromotionType.PERCENT) {
            discount = basePrice.multiply(promotion.getValue()).divide(BigDecimal.valueOf(100), RoundingMode.DOWN);
        } else {
            discount = promotion.getValue();
        }

        var finalPrice = basePrice.subtract(discount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }
        int percentOff = basePrice.signum() == 0 ? 0 : discount.multiply(BigDecimal.valueOf(100)).divide(basePrice, RoundingMode.DOWN).intValue();

        return PromotionApplyResponse.builder()
                .basePrice(basePrice)
                .finalPrice(finalPrice)
                .percentOff(percentOff)
                .promotionId(promotion.getId())
                .promotionName(promotion.getName())
                .build();
    }

    private void validatePromotionNotStarted(Promotion promotion) {
        LocalDateTime now = LocalDateTime.now();
        if (!promotion.getStartAt().isAfter(now)) {
            throw new IllegalArgumentException(
                    "Cannot update promotion: Promotion has already started or ended. " +
                    "Start time: " + promotion.getStartAt() + ", Current time: " + now);
        }
    }

    /**
     * Validate promotion request for CREATE
     * - startAt must be >= now (cannot be in the past)
     */
    private void validatePromotionRequestForCreate(PromotionRequest request) {
        validatePromotionRequest(request);
        
        // Additional validation for CREATE: startAt must be >= now
        LocalDateTime now = LocalDateTime.now();
        
        if (request.getStartAt().isBefore(now)) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, 
                "start_at cannot be in the past");
        }
    }
    
    /**
     * Common validation for promotion request (used by both CREATE and UPDATE)
     */
    private void validatePromotionRequest(PromotionRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (request.getName().length() < 1 || request.getName().length() > 120) {
            throw new IllegalArgumentException("name must be 1..120 chars");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("type is required");
        }
        if (request.getValue() == null) {
            throw new IllegalArgumentException("value is required");
        }
        if (request.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("value must be > 0");
        }
        if (request.getType() == PromotionType.PERCENT && request.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("PERCENT value must be <= 100");
        }
        if (request.getStartAt() == null || request.getEndAt() == null) {
            throw new IllegalArgumentException("startAt and endAt are required");
        }
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new IllegalArgumentException("endAt must be after startAt");
        }
        if (request.getIsActive() == null) {
            request.setIsActive(true);
        }
    }

    private void validateTargetExists(PromotionTargetsUpsertRequest.Item item) {
        if (item.getTargetType() == null || item.getTargetId() == null) {
            throw new IllegalArgumentException("targetType and targetId are required");
        }
        switch (item.getTargetType()) {
            case SKU -> {
                if (!productDetailRepository.existsById(item.getTargetId())) {
                    throw new IllegalArgumentException("SKU (product_detail) not found: " + item.getTargetId());
                }
                if (!productDetailRepository.existsActiveById(item.getTargetId())) {
                    throw new IllegalArgumentException("SKU (product_detail) is not active: " + item.getTargetId());
                }
            }
            case PRODUCT -> {
                if (!productMainRepository.existsById(item.getTargetId())) {
                    throw new IllegalArgumentException("Product not found: " + item.getTargetId());
                }
                if (!productMainRepository.existsActiveById(item.getTargetId())) {
                    throw new IllegalArgumentException("Product is not active: " + item.getTargetId());
                }
            }
            case CATEGORY -> {
                if (!categoryRepository.existsById(item.getTargetId())) {
                    throw new IllegalArgumentException("Category not found: " + item.getTargetId());
                }
                if (!categoryRepository.existsActiveById(item.getTargetId())) {
                    throw new IllegalArgumentException("Category is not active: " + item.getTargetId());
                }
            }
        }
    }

    private void applyRequestToEntity(PromotionRequest request, Promotion p) {
        p.setName(request.getName());
        p.setType(request.getType());
        p.setValue(request.getValue());
        p.setStartAt(request.getStartAt());
        p.setEndAt(request.getEndAt());
        p.setActive(Boolean.TRUE.equals(request.getIsActive()));
    }

    private PromotionResponse toResponse(Promotion p) {
        // Lấy danh sách targets của promotion
        List<PromotionResponse.TargetItem> targets = p.getPromotionTargets().stream()
                .map(t -> PromotionResponse.TargetItem.builder()
                        .targetType(t.getTargetType())
                        .targetId(t.getTargetId())
                        .targetName(getTargetName(t.getTargetType(), t.getTargetId()))
                        .build())
                .toList();

        return PromotionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .type(p.getType())
                .value(p.getValue())
                .startAt(p.getStartAt())
                .endAt(p.getEndAt())
                .isActive(p.isActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .targets(targets)
                .build();
    }

    private String getTargetName(PromotionTargetType targetType, Long targetId) {
        return switch (targetType) {
            case SKU -> productDetailRepository.findById(targetId)
                    .map(pd -> pd.getProduct().getTitle() + " - " + pd.getColor().getName() + " - " + pd.getSize().getLabel())
                    .orElse(null);
            case PRODUCT -> productMainRepository.findById(targetId)
                    .map(product -> product.getTitle())
                    .orElse(null);
            case CATEGORY -> categoryRepository.findById(targetId)
                    .map(category -> category.getName())
                    .orElse(null);
        };
    }

    private Pageable buildPageable(String sort, int page, int pageSize) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        String[] parts = sort.split(",");
        String prop = parts[0];
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, pageSize, Sort.by(dir, prop));
    }

    private void validateTargetItem(PromotionRequest.TargetItem item) {
        if (item.getTargetType() == null || item.getTargetId() == null) {
            throw new IllegalArgumentException("targetType and targetId are required");
        }
        switch (item.getTargetType()) {
            case SKU -> {
                if (!productDetailRepository.existsById(item.getTargetId())) {
                    throw new IllegalArgumentException("SKU (product_detail) not found: " + item.getTargetId());
                }
                if (!productDetailRepository.existsActiveById(item.getTargetId())) {
                    throw new IllegalArgumentException("SKU (product_detail) is not active: " + item.getTargetId());
                }
            }
            case PRODUCT -> {
                if (!productMainRepository.existsById(item.getTargetId())) {
                    throw new IllegalArgumentException("Product not found: " + item.getTargetId());
                }
                if (!productMainRepository.existsActiveById(item.getTargetId())) {
                    throw new IllegalArgumentException("Product is not active: " + item.getTargetId());
                }
            }
            case CATEGORY -> {
                if (!categoryRepository.existsById(item.getTargetId())) {
                    throw new IllegalArgumentException("Category not found: " + item.getTargetId());
                }
                if (!categoryRepository.existsActiveById(item.getTargetId())) {
                    throw new IllegalArgumentException("Category is not active: " + item.getTargetId());
                }
            }
        }
    }

    private void createTargetsForPromotion(Promotion promotion, List<PromotionRequest.TargetItem> targets) {
        Set<PromotionTargetId> seen = new HashSet<>();
        
        for (PromotionRequest.TargetItem item : targets) {
            PromotionTargetId id = new PromotionTargetId(promotion.getId(), item.getTargetType(), item.getTargetId());
            
            // Bỏ qua duplicate trong cùng request
            if (seen.contains(id)) {
                continue;
            }
            seen.add(id);

            PromotionTarget target = PromotionTarget.builder()
                    .id(id)
                    .promotion(promotion)
                    .targetType(item.getTargetType())
                    .targetId(item.getTargetId())
                    .build();
            promotionTargetRepository.save(target);
        }
    }

    /**
     * Kiểm tra tất cả SKUs của target có promotion nào trong period không
     * @param target target cần kiểm tra (SKU, PRODUCT, hoặc CATEGORY)
     * @param startAt thời gian bắt đầu
     * @param endAt thời gian kết thúc
     * @param excludePromotionId promotion ID cần loại trừ (null nếu tạo mới)
     */
    private void validateNoSkuConflict(PromotionRequest.TargetItem target, 
                                       LocalDateTime startAt, 
                                       LocalDateTime endAt, 
                                       Long excludePromotionId) {
        List<Long> skuIds = getSkuIdsFromTarget(target);
        
        for (Long skuId : skuIds) {
            Long conflictingPromotionId;
            if (excludePromotionId == null) {
                conflictingPromotionId = promotionTargetRepository.findConflictingPromotionIdForSku(skuId, startAt, endAt);
            } else {
                conflictingPromotionId = promotionTargetRepository.findConflictingPromotionIdForSkuExcluding(
                        skuId, excludePromotionId, startAt, endAt);
            }
            
            if (conflictingPromotionId != null) {
                throw new IllegalArgumentException(
                        "SKU ID " + skuId + " already has a promotion (Promotion ID: " + conflictingPromotionId + 
                        ") in the specified time period. Please deactivate the existing promotion first.");
            }
        }
    }

    /**
     * Lấy tất cả SKU IDs từ target
     */
    private List<Long> getSkuIdsFromTarget(PromotionRequest.TargetItem target) {
        return switch (target.getTargetType()) {
            case SKU -> List.of(target.getTargetId());
            case PRODUCT -> productDetailRepository.findSkuIdsByProductId(target.getTargetId());
            case CATEGORY -> productDetailRepository.findSkuIdsByCategoryId(target.getTargetId());
        };
    }
}


