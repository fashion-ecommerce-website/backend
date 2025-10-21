package com.spring.fit.backend.promotion.service;

import com.spring.fit.backend.promotion.domain.entity.OrderDetailPromotion;
import com.spring.fit.backend.order.domain.entity.Order;

import java.util.List;

public interface OrderDetailPromotionService {

    List<OrderDetailPromotion> createPromotionsForOrder(Order order);

}
