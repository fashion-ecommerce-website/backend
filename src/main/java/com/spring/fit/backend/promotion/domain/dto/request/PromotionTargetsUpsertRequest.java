package com.spring.fit.backend.promotion.domain.dto.request;

import com.spring.fit.backend.common.enums.PromotionTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionTargetsUpsertRequest {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private PromotionTargetType targetType;
        private Long targetId;
    }

    private List<Item> items;
}



