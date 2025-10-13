package com.spring.fit.backend.review.domain.entity;

import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_detail_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================
    // Quan hệ tới User
    // ==========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // ==========================
    // Quan hệ tới ProductDetail
    // ==========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetail productDetail;

    @Column(nullable = false)
    private Short rating;

    @Column(columnDefinition = "text")
    private String content;

    @Column(
            name = "created_at",
            updatable = false,
            insertable = false,
            columnDefinition = "timestamp default now()"
    )
    private LocalDateTime createdAt;
}