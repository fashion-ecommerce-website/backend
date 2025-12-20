package com.spring.fit.backend.wishlist.repository;

import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.wishlist.domain.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUserAndProductDetail(UserEntity user, ProductDetail productDetail);

    List<Wishlist> findAllByUser(UserEntity user);

    void deleteAllByUser(UserEntity user);
}



