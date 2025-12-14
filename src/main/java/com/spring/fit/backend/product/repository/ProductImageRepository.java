package com.spring.fit.backend.product.repository;

import com.spring.fit.backend.product.domain.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.detail.id = :detailId ORDER BY pi.createdAt")
    List<ProductImage> findByDetailIdOrderByCreatedAt(@Param("detailId") Long detailId);
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.image.url = :imageUrl")
    List<ProductImage> findByImageUrl(@Param("imageUrl") String imageUrl);
    
    @Query("SELECT COUNT(pi) FROM ProductImage pi WHERE pi.detail.id = :detailId")
    long countByDetailId(@Param("detailId") Long detailId);
    
    @Query("""
        SELECT pi.detail.id, pi.image.url 
        FROM ProductImage pi 
        WHERE pi.detail.id IN :detailIds 
        ORDER BY pi.detail.id, pi.createdAt ASC
        """)
    List<Object[]> findAllImageUrlsByDetailIds(@Param("detailIds") List<Long> detailIds);

    @Query("SELECT COUNT(pi) > 0 FROM ProductImage pi WHERE pi.detail.id = :detailId AND pi.image.id = :imageId")
    boolean existsByDetailIdAndImageId(@Param("detailId") Long detailId, @Param("imageId") Long imageId);
}






