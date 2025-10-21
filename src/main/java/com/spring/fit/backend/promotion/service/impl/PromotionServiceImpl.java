package com.spring.fit.backend.promotion.service.impl;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.category.repository.CategoryRepository;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import com.spring.fit.backend.product.repository.ProductRepository;
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
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionTargetRepository promotionTargetRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        validatePromotionRequest(request);

        if (promotionRepository.existsByNameIgnoreCaseAndStartAtAndEndAt(request.getName(), request.getStartAt(), request.getEndAt())) {
            throw new IllegalArgumentException("Promotion name duplicated in the same period");
        }

        Promotion p = new Promotion();
        applyRequestToEntity(request, p);
        Promotion saved = promotionRepository.save(p);
        return toResponse(saved);
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

        applyRequestToEntity(request, p);
        Promotion saved = promotionRepository.save(p);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void toggleActive(Long id) {
        Promotion p = promotionRepository.findById(id).orElseThrow(() -> new RuntimeException("Promotion not found"));
        p.setActive(!p.isActive());
        promotionRepository.save(p);
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
    public PromotionApplyResponse applyBestPromotionForSku(PromotionApplyRequest request) {
        var at = request.getAt() == null ? LocalDateTime.now() : request.getAt();
        var sku = productDetailRepository.findById(request.getSkuId())
                .orElseThrow(() -> new RuntimeException("SKU (product_detail) not found"));

        var basePrice = request.getBasePrice() != null ? request.getBasePrice() : sku.getPrice();

        var activePromotions = promotionRepository.findActiveAt(at);
        if (activePromotions.isEmpty()) {
            return PromotionApplyResponse.builder()
                    .basePrice(basePrice)
                    .finalPrice(basePrice)
                    .percentOff(0)
                    .promotionId(null)
                    .promotionName(null)
                    .build();
        }

        Long productId = sku.getProduct().getId();
        Set<Long> categoryIds = new HashSet<>();
        if (sku.getProduct().getCategories() != null) {
            for (var c : sku.getProduct().getCategories()) {
                categoryIds.add(c.getId().longValue());
            }
        }

        BigDecimal bestDiscount = BigDecimal.ZERO;
        Long bestPromotionId = null;
        String bestPromotionName = null;

        for (var promo : activePromotions) {
            boolean matched = false;
            for (var t : promo.getPromotionTargets()) {
                switch (t.getId().getTargetType()) {
                    case SKU -> matched = t.getId().getTargetId().equals(sku.getId());
                    case PRODUCT -> matched = t.getId().getTargetId().equals(productId);
                    case CATEGORY -> matched = categoryIds.contains(t.getId().getTargetId());
                }
                if (matched) break;
            }
            if (!matched) continue;

            BigDecimal discount;
            if (promo.getType() == PromotionType.PERCENT) {
                discount = basePrice.multiply(promo.getValue()).divide(BigDecimal.valueOf(100));
            } else {
                discount = promo.getValue();
            }
            if (discount.compareTo(bestDiscount) > 0) {
                bestDiscount = discount;
                bestPromotionId = promo.getId();
                bestPromotionName = promo.getName();
            }
        }

        var finalPrice = basePrice.subtract(bestDiscount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }
        int percentOff = basePrice.signum() == 0 ? 0 : bestDiscount.multiply(BigDecimal.valueOf(100)).divide(basePrice, RoundingMode.DOWN).intValue();

        return PromotionApplyResponse.builder()
                .basePrice(basePrice)
                .finalPrice(finalPrice)
                .percentOff(percentOff)
                .promotionId(bestPromotionId)
                .promotionName(bestPromotionName)
                .build();
    }

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
            }
            case PRODUCT -> {
                if (!productRepository.existsById(item.getTargetId())) {
                    throw new IllegalArgumentException("Product not found: " + item.getTargetId());
                }
            }
            case CATEGORY -> {
                if (!categoryRepository.existsById(item.getTargetId())) {
                    throw new IllegalArgumentException("Category not found: " + item.getTargetId());
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
                .build();
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
}


