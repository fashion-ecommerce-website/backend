package com.spring.fit.backend.review.repository;

import com.spring.fit.backend.review.domain.dto.response.ReviewResponse;
import com.spring.fit.backend.review.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductDetailId(Long productDetailId);

    Optional<Review> findByUserIdAndProductDetailId(Long userId, Long productDetailId);

    boolean existsByUserIdAndProductDetailId(Long userId, Long productDetailId);

    List<Review> findByProductDetailIdOrderByCreatedAtDesc(Long productDetailId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
    SELECT r FROM Review r
    JOIN r.productDetail pd
    WHERE pd.product.id = :productId
    ORDER BY r.createdAt DESC
""")
    List<Review> findAllByProductId(@Param("productId") Long productId);


}
