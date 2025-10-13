package com.spring.fit.backend.user.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "user_ranks", indexes = {
        @Index(name = "idx_user_rank_code", columnList = "code", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String code;

    @Column(nullable = false, columnDefinition = "text")
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "timestamp default now()")
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "rank", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<com.spring.fit.backend.voucher.domain.entity.VoucherRankRule> voucherRankRules = new LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRank userRank = (UserRank) o;
        return Objects.equals(id, userRank.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
