package com.spring.fit.backend.refund.repository;

import com.spring.fit.backend.refund.domain.entity.RefundImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundImageRepository extends JpaRepository<RefundImage, Long> {

    @Query("SELECT ri FROM RefundImage ri " +
           "JOIN FETCH ri.image " +
           "WHERE ri.refundRequest.id = :refundRequestId")
    List<RefundImage> findByRefundRequestIdWithImage(@Param("refundRequestId") Long refundRequestId);

    void deleteByRefundRequestId(Long refundRequestId);
}
