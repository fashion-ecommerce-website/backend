package com.spring.fit.backend.cart.repository;

import com.spring.fit.backend.cart.domain.entity.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {

    @Query("""
        SELECT cd FROM CartDetail cd
        JOIN FETCH cd.productDetail pd
        JOIN FETCH pd.product p
        JOIN FETCH pd.color c
        JOIN FETCH pd.size s
        LEFT JOIN FETCH pd.productImages pi
        LEFT JOIN FETCH pi.image
        WHERE cd.user.id = :userId
        ORDER BY cd.createdAt DESC
        """)
    List<CartDetail> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("""
        SELECT cd FROM CartDetail cd
        WHERE cd.user.id = :userId AND cd.productDetail.id = :productDetailId
        """)
    Optional<CartDetail> findByUserIdAndProductDetailId(@Param("userId") Long userId, 
                                                        @Param("productDetailId") Long productDetailId);

    @Query("""
        SELECT cd FROM CartDetail cd
        JOIN FETCH cd.productDetail pd
        JOIN FETCH pd.product p
        JOIN FETCH pd.color c
        JOIN FETCH pd.size s
        WHERE cd.id = :cartDetailId AND cd.user.id = :userId
        """)
    Optional<CartDetail> findByIdAndUserId(@Param("cartDetailId") Long cartDetailId, 
                                          @Param("userId") Long userId);

    @Modifying
    @Query("""
        DELETE FROM CartDetail cd
        WHERE cd.id IN :cartDetailIds AND cd.user.id = :userId
        """)
    int deleteByIdsAndUserId(@Param("cartDetailIds") List<Long> cartDetailIds, 
                            @Param("userId") Long userId);

    @Modifying
    @Query("""
        DELETE FROM CartDetail cd
        WHERE cd.user.id = :userId
        """)
    int deleteAllByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(cd.quantity), 0) FROM CartDetail cd WHERE cd.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

}
