package com.spring.fit.backend.promotion.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionApplyResponse {
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
    private Integer percentOff;     // làm tròn xuống %, để hiển thị
    private Long promotionId;       // null nếu không có rule
    private String promotionName;   // null nếu không có rule
}



