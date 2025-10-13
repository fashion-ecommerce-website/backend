package com.spring.fit.backend.voucher.domain.entity;

import com.spring.fit.backend.common.enums.VoucherUsageStatus;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "voucher_usages", 
       indexes = {
           @Index(name = "idx_voucher_usage_voucher_id", columnList = "voucher_id"),
           @Index(name = "idx_voucher_usage_user_id", columnList = "user_id"),
           @Index(name = "idx_voucher_usage_order_id", columnList = "order_id"),
           @Index(name = "idx_voucher_usage_status", columnList = "status"),
           @Index(name = "idx_voucher_usage_used_at", columnList = "used_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_voucher_usage_order", columnNames = {"order_id"})
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false, foreignKey = @ForeignKey(name = "fk_voucher_usage_voucher"))
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_voucher_usage_user"))
    private UserEntity user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "fk_voucher_usage_order"))
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VoucherUsageStatus status = VoucherUsageStatus.APPLIED;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "used_at")
    @Builder.Default
    private LocalDateTime usedAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoucherUsage that = (VoucherUsage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
