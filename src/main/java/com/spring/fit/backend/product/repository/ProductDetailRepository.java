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
    
    @Query("""
        SELECT pd FROM ProductDetail pd
        LEFT JOIN FETCH pd.product p
        LEFT JOIN FETCH p.categories
        LEFT JOIN FETCH pd.color
        LEFT JOIN FETCH pd.size
        WHERE pd.id = :id AND pd.isActive = true
        """)
    Optional<ProductDetail> findActiveProductDetailByIdWithRelations(@Param("id") Long id);
    
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.color.id = :colorId AND pd.size.id = :sizeId")
    Optional<ProductDetail> findByProductAndColorAndSize(@Param("productId") Long productId, 
                                                         @Param("colorId") Short colorId, 
                                                         @Param("sizeId") Short sizeId);

    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.color.id = :colorId AND pd.size.id = :sizeId AND pd.isActive = true")
    Optional<ProductDetail> findByActiveProductAndColorAndSize(@Param("productId") Long productId,
                                                         @Param("colorId") Short colorId,
                                                         @Param("sizeId") Short sizeId);
    
    @Query("""
        SELECT pd FROM ProductDetail pd
        LEFT JOIN FETCH pd.product p
        LEFT JOIN FETCH p.categories
        LEFT JOIN FETCH pd.color
        LEFT JOIN FETCH pd.size
        WHERE pd.product.id = :productId AND pd.color.id = :colorId AND pd.size.id = :sizeId AND pd.isActive = true
        """)
    Optional<ProductDetail> findByActiveProductAndColorAndSizeWithRelations(@Param("productId") Long productId,
                                                         @Param("colorId") Short colorId,
                                                         @Param("sizeId") Short sizeId);
    
    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.isActive = true")
    List<ProductDetail> findActiveDetailsByProductId(@Param("productId") Long productId);
    
    @Query("""
        SELECT DISTINCT pd FROM ProductDetail pd
        LEFT JOIN FETCH pd.product p
        LEFT JOIN FETCH p.categories
        LEFT JOIN FETCH pd.color
        LEFT JOIN FETCH pd.size
        WHERE pd.product.id = :productId AND pd.isActive = true
        ORDER BY pd.price ASC
        """)
    List<ProductDetail> findActiveDetailsByProductIdWithProduct(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(pd) FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.isActive = true")
    long countActiveDetailsByProductId(@Param("productId") Long productId);
    
    @Query("SELECT pd.product.id FROM ProductDetail pd WHERE pd.id = :detailId")
    Optional<Long> findProductIdByDetailId(@Param("detailId") Long detailId);
    
    @Query(value = """
        SELECT i.url
        FROM product_images pi
        JOIN images i ON i.id = pi.image_id
        WHERE pi.detail_id = :detailId
        ORDER BY pi.created_at ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<String> findFirstImageUrlByDetailId(@Param("detailId") Long detailId);
    
    @Query("""
        SELECT pd FROM ProductDetail pd
        LEFT JOIN FETCH pd.product p
        LEFT JOIN FETCH p.categories
        LEFT JOIN FETCH pd.color c
        LEFT JOIN FETCH pd.size s
        WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))
        AND LOWER(c.name) = LOWER(:colorName)
        AND LOWER(s.label) = LOWER(:sizeLabel)
        AND pd.isActive = true
        ORDER BY pd.id ASC
        """)
    List<ProductDetail> findByProductTitleAndColorAndSize(
            @Param("title") String title,
            @Param("colorName") String colorName,
            @Param("sizeLabel") String sizeLabel);


    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
            "FROM ProductDetail d " +
            "WHERE d.product.title = :title " +
            "AND LOWER(d.color.name) = LOWER(:color) " +
            "AND LOWER(d.size.code) = LOWER(:size)")
    boolean existsByProductTitleAndColorAndSize(@Param("title") String title,
                                                @Param("color") String color,
                                                @Param("size") String size);

    @Query("SELECT pd.id FROM ProductDetail pd WHERE pd.product.id = :productId")
    List<Long> findSkuIdsByProductId(@Param("productId") Long productId);

    @Query(value = """
        SELECT pd.id FROM product_details pd
        JOIN product_categories pc ON pd.product_id = pc.product_id
        WHERE pc.category_id = :categoryId
    """, nativeQuery = true)
    List<Long> findSkuIdsByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(pd) > 0 FROM ProductDetail pd WHERE pd.id = :id AND pd.isActive = true AND pd.product.isActive = true")
    boolean existsActiveById(@Param("id") Long id);
}






