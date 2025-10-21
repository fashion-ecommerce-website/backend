package com.spring.fit.backend.voucher.repository;

import com.spring.fit.backend.common.enums.VoucherUsageStatus;
import com.spring.fit.backend.voucher.domain.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {

    Optional<VoucherUsage> findByOrderId(Long orderId);

    @Query("SELECT COUNT(vu) FROM VoucherUsage vu WHERE vu.voucher.id = :voucherId AND vu.user.id = :userId AND vu.status = :status")
    Long countUsageByUser(@Param("voucherId") Long voucherId, @Param("userId") Long userId, @Param("status") VoucherUsageStatus status);

    @Query("SELECT COUNT(vu) FROM VoucherUsage vu WHERE vu.voucher.id = :voucherId AND vu.status = :status")
    Long countTotalUsage(@Param("voucherId") Long voucherId, @Param("status") VoucherUsageStatus status);

}


