package com.spring.fit.backend.wishlist.repository;

import com.spring.fit.backend.wishlist.domain.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUserIdAndDetailId(Long userId, Long detailId);
    List<Wishlist> findAllByUserId(Long userId);
}



