package com.spring.fit.backend.promotion.repository;

import com.spring.fit.backend.promotion.domain.entity.Promotion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {
    boolean existsByNameIgnoreCaseAndStartAtAndEndAt(String name, java.time.LocalDateTime startAt, java.time.LocalDateTime endAt);

    @Query("select p from Promotion p where p.isActive = true and p.startAt <= :at and p.endAt >= :at")
    List<Promotion> findActiveAt(java.time.LocalDateTime at);
}


