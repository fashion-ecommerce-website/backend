package com.spring.fit.backend.voucher.domain.entity;

import com.spring.fit.backend.user.domain.entity.UserRank;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "voucher_rank_rules", 
       indexes = {
           @Index(name = "idx_voucher_rank_rule_voucher_id", columnList = "voucher_id"),
           @Index(name = "idx_voucher_rank_rule_rank_id", columnList = "rank_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_voucher_rank_rule", columnNames = {"voucher_id", "rank_id"})
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherRankRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false, foreignKey = @ForeignKey(name = "fk_voucher_rank_rule_voucher"))
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rank_id", nullable = false, foreignKey = @ForeignKey(name = "fk_voucher_rank_rule_rank"))
    private UserRank rank;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoucherRankRule that = (VoucherRankRule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
