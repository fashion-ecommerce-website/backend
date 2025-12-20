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

    @Query("""
    select r from Review r
    join r.orderDetail od
    join od.productDetail pd
    where pd.id = :productDetailId
    order by r.createdAt desc
""")
    List<Review> findReviewsByProductDetailId(
            @Param("productDetailId") Long productDetailId
    );

    @Query("""
    select r from Review r
    join r.orderDetail od
    where od.order.user.id = :userId
    order by r.createdAt desc
""")
    List<Review> findUserReviews(
            @Param("userId") Long userId
    );


    @Query("""
    select r from Review r
    join r.orderDetail od
    join od.productDetail pd
    where pd.product.id = :productId
    order by r.createdAt desc
""")
    List<Review> findAllByProductId(
            @Param("productId") Long productId
    );

}
