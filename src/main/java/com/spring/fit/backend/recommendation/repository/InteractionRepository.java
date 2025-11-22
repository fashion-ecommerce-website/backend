package com.spring.fit.backend.recommendation.repository;

import com.spring.fit.backend.recommendation.domain.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    @Query("SELECT i FROM Interaction i WHERE i.user.id = :userId AND i.product.id = :productId AND i.actionType = :actionType")
    Optional<Interaction> findByUserIdAndProductIdAndActionType(
            @Param("userId") Long userId, 
            @Param("productId") Long productId, 
            @Param("actionType") String actionType);

    @Query("SELECT i FROM Interaction i WHERE i.user.id = :userId")
    List<Interaction> findByUserId(@Param("userId") Long userId);

    @Query("SELECT i FROM Interaction i WHERE i.product.id = :productId")
    List<Interaction> findByProductId(@Param("productId") Long productId);

    @Query("SELECT i FROM Interaction i WHERE i.user.id = :userId")
    List<Interaction> findAllByUserId(@Param("userId") Long userId);
}
