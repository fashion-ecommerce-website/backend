package com.spring.fit.backend.recommendation.repository;

import com.spring.fit.backend.recommendation.domain.entity.ProductMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMetadataRepository extends JpaRepository<ProductMetadata, Long> {
    Optional<ProductMetadata> findByProductId(Long productId);
    boolean existsByProductId(Long productId);
    
    @Query("SELECT pm FROM ProductMetadata pm WHERE pm.productId IN :productIds")
    List<ProductMetadata> findByProductIdIn(@Param("productIds") List<Long> productIds);
}
