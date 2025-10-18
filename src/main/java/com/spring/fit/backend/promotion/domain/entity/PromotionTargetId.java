package com.spring.fit.backend.promotion.domain.entity;

import com.spring.fit.backend.common.enums.PromotionTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PromotionTargetId implements Serializable {

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private PromotionTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;
}



