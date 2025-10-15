package com.spring.fit.backend.promotion.domain.dto.response;

import com.spring.fit.backend.common.enums.PromotionTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionTargetResponse {
    private Long promotionId;
    private PromotionTargetType targetType;
    private Long targetId;
}



