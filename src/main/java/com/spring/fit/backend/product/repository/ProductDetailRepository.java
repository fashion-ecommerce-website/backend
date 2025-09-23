package com.spring.fit.backend.product.repository;

import com.spring.fit.backend.product.domain.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {
    
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.id = :id AND pd.isActive = true")
    Optional<ProductDetail> findActiveProductDetailById(@Param("id") Long id);
    
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.color.id = :colorId AND pd.size.id = :sizeId")
    Optional<ProductDetail> findByProductAndColorAndSize(@Param("productId") Long productId, 
                                                         @Param("colorId") Short colorId, 
                                                         @Param("sizeId") Short sizeId);
    
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.isActive = true")
    List<ProductDetail> findActiveDetailsByProductId(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(pd) FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.isActive = true")
    long countActiveDetailsByProductId(@Param("productId") Long productId);
}






