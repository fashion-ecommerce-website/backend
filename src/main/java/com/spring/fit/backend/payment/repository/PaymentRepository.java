package com.spring.fit.backend.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.spring.fit.backend.payment.domain.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findFirstByOrder_IdOrderByIdDesc(Long orderId);

    @Query(value = "select o.id as order_id, o.total_amount, o.subtotal_amount, o.discount_amount, o.shipping_fee, o.currency, od.title, od.color_label, od.size_label, od.quantity, od.unit_price "
            + "from payments p join orders o on p.order_id = o.id join order_details od on od.order_id = o.id "
            + "where p.id = :paymentId", nativeQuery = true)
    List<Object[]> findOrderAndItemsByPaymentId(@Param("paymentId") Long paymentId);

    @Query(value = "select o.user_id from payments p join orders o on p.order_id = o.id where p.id = :paymentId", nativeQuery = true)
    Optional<Long> findUserIdByPaymentId(@Param("paymentId") Long paymentId);
}


