package com.spring.fit.backend.product.repository;

import com.spring.fit.backend.product.domain.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Short> {
    Optional<Size> findByCode(String code);

    boolean existsByCodeIgnoreCase(String code);

    Optional<Size> findByCodeIgnoreCase(String code);

    @Query("SELECT s FROM Size s WHERE s.isActive = true")
    List<Size> findAllActiveSizes();

    @Query("""
        SELECT COUNT(pd) > 0
        FROM ProductDetail pd
        WHERE pd.size.id = :sizeId
          AND pd.quantity > 0
          AND pd.isActive = true
    """)
    boolean existsActiveProductDetailWithQuantity(Short sizeId);
}






