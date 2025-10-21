package com.spring.fit.backend.promotion.service.impl;

import com.spring.fit.backend.common.enums.PromotionType;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.domain.entity.OrderDetail;
import com.spring.fit.backend.promotion.domain.entity.OrderDetailPromotion;
import com.spring.fit.backend.promotion.domain.entity.OrderDetailPromotionId;
import com.spring.fit.backend.promotion.domain.entity.Promotion;
import com.spring.fit.backend.promotion.repository.OrderDetailPromotionRepository;
import com.spring.fit.backend.promotion.repository.PromotionRepository;
import com.spring.fit.backend.promotion.service.OrderDetailPromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderDetailPromotionServiceImpl implements OrderDetailPromotionService {

    private final OrderDetailPromotionRepository orderDetailPromotionRepository;
    private final PromotionRepository promotionRepository;

    @Override
    public List<OrderDetailPromotion> createPromotionsForOrder(Order order) {
        log.info("Inside OrderDetailPromotionServiceImpl.createPromotionsForOrder, Creating promotions for order: {}", order.getId());
        
        List<OrderDetailPromotion> createdPromotions = new ArrayList<>();
        
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            if (orderDetail.getPromotionId() != null) {
                try {
                    OrderDetailPromotion promotion = createPromotionForOrderDetail(order, orderDetail);
                    createdPromotions.add(promotion);
                } catch (Exception e) {
                    log.error("Inside OrderDetailPromotionServiceImpl.createPromotionsForOrder, Failed to create promotion for order detail: {} with promotion: {}", 
                        orderDetail.getId(), orderDetail.getPromotionId(), e);
                }
            }
        }
        
        log.info("Inside OrderDetailPromotionServiceImpl.createPromotionsForOrder, Created {} promotions for order: {}", createdPromotions.size(), order.getId());
        return createdPromotions;
    }

    private OrderDetailPromotion createPromotionForOrderDetail(Order order, OrderDetail orderDetail) {
        // Get promotion details
        Promotion promotion = promotionRepository.findById(orderDetail.getPromotionId())
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + orderDetail.getPromotionId()));

        // Calculate discount amount
        BigDecimal discountAmount = calculateDiscountAmount(promotion, orderDetail);

        // Create composite key
        OrderDetailPromotionId id = OrderDetailPromotionId.builder()
                .orderId(order.getId())
                .detailId(orderDetail.getId())
                .build();

        // Create OrderDetailPromotion entity
        OrderDetailPromotion orderDetailPromotion = OrderDetailPromotion.builder()
                .id(id)
                .order(order)
                .detail(orderDetail.getProductDetail())
                .promotion(promotion)
                .promotionName(promotion.getName())
                .discountAmount(discountAmount)
                .build();

        return orderDetailPromotionRepository.save(orderDetailPromotion);
    }

    private BigDecimal calculateDiscountAmount(Promotion promotion, OrderDetail orderDetail) {
        BigDecimal basePrice = orderDetail.getUnitPrice();
        BigDecimal quantity = BigDecimal.valueOf(orderDetail.getQuantity());
        BigDecimal totalPrice = basePrice.multiply(quantity);

        BigDecimal discountAmount;
        if (promotion.getType() == PromotionType.PERCENT) {
            // Calculate percentage discount
            discountAmount = totalPrice.multiply(promotion.getValue()).divide(BigDecimal.valueOf(100));
        } else {
            // Fixed amount discount
            discountAmount = promotion.getValue();
        }

        // Ensure discount doesn't exceed total price
        if (discountAmount.compareTo(totalPrice) > 0) {
            discountAmount = totalPrice;
        }

        return discountAmount;
    }

}
