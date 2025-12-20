package com.spring.fit.backend.promotion.repository;

import com.spring.fit.backend.promotion.domain.entity.Promotion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.startAt <= :at AND p.endAt >= :at")
    List<Promotion> findActiveAt(LocalDateTime at);

    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isActive = true AND p.endAt < :now")
    long countExpiredActivePromotions(LocalDateTime now);

    @Modifying
    @Query("UPDATE Promotion p SET p.isActive = false WHERE p.isActive = true AND p.endAt < :now")
    int deactivateExpiredPromotions(LocalDateTime now);
}


