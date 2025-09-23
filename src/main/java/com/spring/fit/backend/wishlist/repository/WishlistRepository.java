package com.spring.fit.backend.wishlist.repository;

import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.wishlist.domain.entity.Wishlist;
import com.spring.fit.backend.wishlist.domain.entity.WishlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {

    List<Wishlist> findByUser_Id(Long userId);

    Optional<Wishlist> findByUser_IdAndProductDetail_Id(Long userId, Long detailId);


}


