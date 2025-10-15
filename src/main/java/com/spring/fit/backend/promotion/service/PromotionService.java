package com.spring.fit.backend.promotion.service;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionRequest;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionTargetsRemoveRequest;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionTargetsUpsertRequest;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionApplyRequest;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionResponse;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionTargetResponse;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionApplyResponse;
import com.spring.fit.backend.promotion.domain.dto.response.TargetsUpsertResult;
import com.spring.fit.backend.promotion.domain.entity.PromotionTarget;
import com.spring.fit.backend.common.enums.PromotionTargetType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface PromotionService {

    PromotionResponse create(PromotionRequest request);

    PromotionResponse getById(Long id);

    PageResult<PromotionResponse> search(String name,
                                         Boolean isActive,
                                         LocalDateTime from,
                                         LocalDateTime to,
                                         String type,
                                         int page,
                                         int pageSize,
                                         String sort);

    PromotionResponse update(Long id, PromotionRequest request);

    void toggleActive(Long id);

    TargetsUpsertResult upsertTargets(Long promotionId, PromotionTargetsUpsertRequest request);

    PageResult<PromotionTargetResponse> listTargets(Long promotionId, PromotionTargetType type, int page, int pageSize);

    long removeTargets(Long promotionId, PromotionTargetsRemoveRequest request);

    PromotionApplyResponse applyBestPromotionForSku(PromotionApplyRequest request);
}


