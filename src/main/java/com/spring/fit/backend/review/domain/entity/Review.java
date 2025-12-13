package com.spring.fit.backend.review.domain.entity;

import com.spring.fit.backend.order.domain.entity.OrderDetail;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @Column(name = "order_detail_id")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetail orderDetail;

    @Column(
            nullable = false,
            precision = 2, // tổng số chữ số
            scale = 2      // số chữ số thập phân
    )
    private BigDecimal rating;

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

