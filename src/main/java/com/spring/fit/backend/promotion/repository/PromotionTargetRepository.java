package com.spring.fit.backend.promotion.repository;

import com.spring.fit.backend.promotion.domain.entity.PromotionTarget;
import com.spring.fit.backend.promotion.domain.entity.PromotionTargetId;
import com.spring.fit.backend.common.enums.PromotionTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionTargetRepository extends JpaRepository<PromotionTarget, PromotionTargetId> {

    @Query("select count(t) > 0 from PromotionTarget t where t.promotion.id = :promotionId")
    boolean existsByPromotionId(Long promotionId);

    Page<PromotionTarget> findByPromotionId(Long promotionId, Pageable pageable);

    Page<PromotionTarget> findByPromotionIdAndTargetType(Long promotionId, PromotionTargetType type, Pageable pageable);

    long deleteByPromotion_Id(Long promotionId);

    /**
     * Tìm promotion ID đang conflict với SKU trong period
     * (bao gồm promotion target trực tiếp SKU, Product chứa SKU, hoặc Category chứa Product chứa SKU)
     */
    @Query(value = """
        SELECT p.id FROM promotion_targets pt
        JOIN promotions p ON pt.promotion_id = p.id
        WHERE p.start_at <= :endAt AND p.end_at >= :startAt
        AND p.is_active = true
        AND (
            (pt.target_type = 'SKU' AND pt.target_id = :skuId)
            OR (pt.target_type = 'PRODUCT' AND pt.target_id = (SELECT product_id FROM product_details WHERE id = :skuId))
            OR (pt.target_type = 'CATEGORY' AND pt.target_id IN (
                SELECT pc.category_id FROM product_categories pc 
                WHERE pc.product_id = (SELECT product_id FROM product_details WHERE id = :skuId)
            ))
        )
        LIMIT 1
    """, nativeQuery = true)
    Long findConflictingPromotionIdForSku(Long skuId, LocalDateTime startAt, LocalDateTime endAt);

    /**
     * Tìm promotion ID đang conflict với SKU trong period (loại trừ promotion cụ thể - dùng khi update)
     */
    @Query(value = """
        SELECT p.id FROM promotion_targets pt
        JOIN promotions p ON pt.promotion_id = p.id
        WHERE p.id <> :excludePromotionId
        AND p.is_active = true
        AND p.start_at <= :endAt AND p.end_at >= :startAt
        AND (
            (pt.target_type = 'SKU' AND pt.target_id = :skuId)
            OR (pt.target_type = 'PRODUCT' AND pt.target_id = (SELECT product_id FROM product_details WHERE id = :skuId))
            OR (pt.target_type = 'CATEGORY' AND pt.target_id IN (
                SELECT pc.category_id FROM product_categories pc 
                WHERE pc.product_id = (SELECT product_id FROM product_details WHERE id = :skuId)
            ))
        )
        LIMIT 1
    """, nativeQuery = true)
    Long findConflictingPromotionIdForSkuExcluding(Long skuId, Long excludePromotionId, 
                                                    LocalDateTime startAt, LocalDateTime endAt);

    @Query(value = """
        SELECT p.id FROM promotion_targets pt
        JOIN promotions p ON pt.promotion_id = p.id
        WHERE p.is_active = true
        AND p.start_at <= :at AND p.end_at >= :at
        AND (
            (pt.target_type = 'SKU' AND pt.target_id = :skuId)
            OR (pt.target_type = 'PRODUCT' AND pt.target_id = :productId)
            OR (pt.target_type = 'CATEGORY' AND pt.target_id IN (:categoryIds))
        )
        LIMIT 1
    """, nativeQuery = true)
    Long findPromotionIdForSkuAt(Long skuId, Long productId, List<Long> categoryIds, LocalDateTime at);
}



