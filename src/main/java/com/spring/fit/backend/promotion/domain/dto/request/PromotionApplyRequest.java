package com.spring.fit.backend.promotion.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionApplyRequest {
    private Long skuId;                 // product_detail id
    private BigDecimal basePrice;       // optional; nếu null sẽ lấy theo DB
    private LocalDateTime at;           // optional; nếu null dùng now
}



