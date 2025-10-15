package com.spring.fit.backend.promotion.controller;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionRequest;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionTargetsRemoveRequest;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionTargetsUpsertRequest;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionResponse;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionTargetResponse;
import com.spring.fit.backend.promotion.domain.dto.response.TargetsUpsertResult;
import com.spring.fit.backend.promotion.domain.entity.PromotionTarget;
import com.spring.fit.backend.common.enums.PromotionTargetType;
import com.spring.fit.backend.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionResponse> create(@RequestBody PromotionRequest request) {
        PromotionResponse res = promotionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResult<PromotionResponse>> search(@RequestParam(required = false) String name,
                                                                @RequestParam(required = false) Boolean isActive,
                                                                @RequestParam(required = false) LocalDateTime from,
                                                                @RequestParam(required = false) LocalDateTime to,
                                                                @RequestParam(required = false) String type,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int pageSize,
                                                                @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(promotionService.search(name, isActive, from, to, type, page, pageSize, sort));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> update(@PathVariable Long id, @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(promotionService.update(id, request));
    }

    @PostMapping("/{id}:toggle")
    public ResponseEntity<Void> toggle(@PathVariable Long id) {
        promotionService.toggleActive(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/targets:upsert")
    public ResponseEntity<TargetsUpsertResult> upsertTargets(@PathVariable Long id, @RequestBody PromotionTargetsUpsertRequest request) {
        return ResponseEntity.ok(promotionService.upsertTargets(id, request));
    }

    @GetMapping("/{id}/targets")
    public ResponseEntity<PageResult<PromotionTargetResponse>> listTargets(@PathVariable Long id,
                                                                           @RequestParam(required = false) PromotionTargetType type,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "50") int pageSize) {
        return ResponseEntity.ok(promotionService.listTargets(id, type, page, pageSize));
    }

    @DeleteMapping("/{id}/targets")
    public ResponseEntity<Long> removeTargets(@PathVariable Long id, @RequestBody PromotionTargetsRemoveRequest request) {
        long removed = promotionService.removeTargets(id, request);
        return ResponseEntity.ok(removed);
    }
}


