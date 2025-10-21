package com.spring.fit.backend.promotion.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailPromotionId implements Serializable {

    private Long orderId;
    private Long detailId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDetailPromotionId that = (OrderDetailPromotionId) o;
        return Objects.equals(orderId, that.orderId) && Objects.equals(detailId, that.detailId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, detailId);
    }
}