package com.spring.fit.backend.wishlist.domain.entity;

import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(WishlistId.class)
public class Wishlist {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_id", nullable = false)
    private ProductDetail productDetail;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
