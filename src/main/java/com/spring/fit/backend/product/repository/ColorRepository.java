package com.spring.fit.backend.product.repository;

import com.spring.fit.backend.product.domain.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Short> {
    Optional<Color> findByName(String name);
    boolean existsByNameIgnoreCase(String name);
    Optional<Color> findByNameIgnoreCase(String name);
    @Query("SELECT c FROM Color c WHERE c.isActive = true")
    List<Color> findAllActiveColors();

    @Query("""
        SELECT COUNT(pd) > 0
        FROM ProductDetail pd
        WHERE pd.color.id = :colorId
          AND pd.quantity > 0
          AND pd.isActive = true
    """)
    boolean existsActiveProductDetailWithQuantity(Short colorId);
}






