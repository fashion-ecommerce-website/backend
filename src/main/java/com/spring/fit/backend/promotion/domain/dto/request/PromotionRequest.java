package com.spring.fit.backend.promotion.domain.dto.request;

import com.spring.fit.backend.common.enums.PromotionTargetType;
import com.spring.fit.backend.common.enums.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {
    private String name;
    private PromotionType type;
    private BigDecimal value;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Boolean isActive;
    
    /**
     * Danh sách targets cho promotion (SKU, PRODUCT, hoặc CATEGORY)
     */
    private List<TargetItem> targets;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetItem {
        private PromotionTargetType targetType;
        private Long targetId;
    }
}



