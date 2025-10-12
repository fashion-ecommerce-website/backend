package com.spring.fit.backend.voucher.repository;

import com.spring.fit.backend.voucher.domain.entity.VoucherRankRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRankRuleRepository extends JpaRepository<VoucherRankRule, Long> {

    List<VoucherRankRule> findByVoucherId(Long voucherId);

    @Query("SELECT COUNT(vrr) > 0 FROM VoucherRankRule vrr WHERE vrr.voucher.id = :voucherId AND vrr.rank.id = :rankId")
    boolean existsByVoucherIdAndRankId(@Param("voucherId") Long voucherId, @Param("rankId") Short rankId);

    @Query("SELECT vrr.rank.id FROM VoucherRankRule vrr WHERE vrr.voucher.id = :voucherId")
    List<Short> findRankIdsByVoucherId(@Param("voucherId") Long voucherId);

    void deleteByVoucherId(Long voucherId);

    void deleteByVoucherIdAndRankId(Long voucherId, Short rankId);
}


