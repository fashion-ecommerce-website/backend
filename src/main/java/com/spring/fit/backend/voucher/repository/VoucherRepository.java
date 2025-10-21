package com.spring.fit.backend.voucher.repository;

import com.spring.fit.backend.voucher.domain.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {

    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.isActive = true " +
           "AND v.startAt <= :now AND v.endAt >= :now " +
           "AND (:subtotal IS NULL OR v.minOrderAmount IS NULL OR v.minOrderAmount <= :subtotal)")
    Optional<Voucher> findValidVoucherByCode(@Param("code") String code, 
                                           @Param("now") LocalDateTime now, 
                                           @Param("subtotal") Double subtotal);

    boolean existsByCodeIgnoreCase(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.isActive = true " +
           "AND v.startAt <= :now AND v.endAt >= :now " +
           "AND (:subtotal IS NULL OR v.minOrderAmount IS NULL OR v.minOrderAmount <= :subtotal)")
    Optional<Voucher> findValidVoucherByCodeForUpdate(@Param("code") String code,
                                                     @Param("now") LocalDateTime now,
                                                     @Param("subtotal") Double subtotal);

    @Query("SELECT v FROM Voucher v WHERE LOWER(v.code) LIKE LOWER(CONCAT('%', :searchCode, '%')) AND v.isActive = true")
    List<Voucher> findByCodeContainingIgnoreCase(@Param("searchCode") String searchCode);
}


