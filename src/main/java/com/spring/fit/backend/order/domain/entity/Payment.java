package com.spring.fit.backend.order.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import com.spring.fit.backend.common.enums.PaymentMethod;
import com.spring.fit.backend.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_order_id", columnList = "order_id"),
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.UNPAID;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "provider", length = 100)
    private String provider;

    @Column(name = "transaction_no", length = 255)
    private String transactionNo;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

