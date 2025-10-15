package com.spring.fit.backend.promotion.repository;

import com.spring.fit.backend.promotion.domain.entity.OrderDetailPromotion;
import com.spring.fit.backend.promotion.domain.entity.OrderDetailPromotionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailPromotionRepository extends JpaRepository<OrderDetailPromotion, OrderDetailPromotionId> {
}



