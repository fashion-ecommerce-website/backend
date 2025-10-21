package com.spring.fit.backend.promotion.domain.entity;

import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(
        name = "order_detail_promotions",
        indexes = {
                @Index(name = "idx_odp_promotion_id", columnList = "promotion_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailPromotion {

    @EmbeddedId
    private OrderDetailPromotionId id;

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @MapsId("detailId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_id")
    private ProductDetail detail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion; 

    @Column(name = "promotion_name", columnDefinition = "text")
    private String promotionName;

    @Column(name = "discount_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal discountAmount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDetailPromotion that = (OrderDetailPromotion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}



