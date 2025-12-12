package com.spring.fit.backend.refund.repository;

import com.spring.fit.backend.common.enums.RefundStatus;
import com.spring.fit.backend.refund.domain.entity.RefundRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    Page<RefundRequest> findByUserId(Long userId, Pageable pageable);

    Page<RefundRequest> findByUserIdAndStatus(Long userId, RefundStatus status, Pageable pageable);

    Page<RefundRequest> findByStatus(RefundStatus status, Pageable pageable);

    @Query("SELECT r FROM RefundRequest r WHERE r.order.id = :orderId AND r.status = :status")
    Optional<RefundRequest> findByOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") RefundStatus status);

    @Query("SELECT r FROM RefundRequest r WHERE r.order.id = :orderId")
    List<RefundRequest> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT r FROM RefundRequest r WHERE r.status = :status")
    Page<RefundRequest> findAllByStatus(@Param("status") RefundStatus status, Pageable pageable);

    @Query("SELECT r FROM RefundRequest r JOIN FETCH r.order JOIN FETCH r.user WHERE r.id = :id")
    Optional<RefundRequest> findByIdWithRelations(@Param("id") Long id);
}



