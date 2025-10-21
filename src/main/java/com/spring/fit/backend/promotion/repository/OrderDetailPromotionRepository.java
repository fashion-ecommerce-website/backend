package com.spring.fit.backend.promotion.repository;

import com.spring.fit.backend.promotion.domain.entity.OrderDetailPromotion;
import com.spring.fit.backend.promotion.domain.entity.OrderDetailPromotionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailPromotionRepository extends JpaRepository<OrderDetailPromotion, OrderDetailPromotionId> {

    @Query("SELECT odp FROM OrderDetailPromotion odp WHERE odp.order.id = :orderId")
    List<OrderDetailPromotion> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT odp FROM OrderDetailPromotion odp WHERE odp.detail.id = :detailId")
    List<OrderDetailPromotion> findByDetailId(@Param("detailId") Long detailId);

    @Query("SELECT COUNT(odp) FROM OrderDetailPromotion odp WHERE odp.promotion.id = :promotionId")
    Long countByPromotionId(@Param("promotionId") Long promotionId);
}