package com.spring.fit.backend.promotion.domain.entity;

import com.spring.fit.backend.common.enums.PromotionTargetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(
        name = "promotion_targets",
        indexes = {
                @Index(name = "idx_promotion_targets_by_promotion", columnList = "promotion_id"),
                @Index(name = "idx_promotion_targets_match", columnList = "target_type, target_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionTarget {

    @EmbeddedId
    private PromotionTargetId id;

    @MapsId("promotionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, insertable = false, updatable = false)
    private PromotionTargetType targetType;

    @Column(name = "target_id", nullable = false, insertable = false, updatable = false)
    private Long targetId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromotionTarget that = (PromotionTarget) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}



