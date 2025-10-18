package com.spring.fit.backend.promotion.repository;

import com.spring.fit.backend.promotion.domain.entity.PromotionTarget;
import com.spring.fit.backend.promotion.domain.entity.PromotionTargetId;
import com.spring.fit.backend.common.enums.PromotionTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PromotionTargetRepository extends JpaRepository<PromotionTarget, PromotionTargetId> {

    @Query("select count(t) > 0 from PromotionTarget t where t.promotion.id = :promotionId")
    boolean existsByPromotionId(Long promotionId);

    Page<PromotionTarget> findByPromotionId(Long promotionId, Pageable pageable);

    Page<PromotionTarget> findByPromotionIdAndTargetType(Long promotionId, PromotionTargetType type, Pageable pageable);

    long deleteByPromotion_Id(Long promotionId);
}



