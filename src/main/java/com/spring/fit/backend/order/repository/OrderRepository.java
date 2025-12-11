package com.spring.fit.backend.order.repository;

import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.common.enums.FulfillmentStatus;
import com.spring.fit.backend.common.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

        // Find orders by user
        Page<Order> findByUserId(Long userId, Pageable pageable);

        // Find orders by status
        Page<Order> findByStatus(FulfillmentStatus status, Pageable pageable);

        // Find orders by payment status
        Page<Order> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

        // Find orders by user and status
        Page<Order> findByUserIdAndStatus(Long userId, FulfillmentStatus status, Pageable pageable);

        // Find orders by user and payment status
        Page<Order> findByUserIdAndPaymentStatus(Long userId, PaymentStatus paymentStatus, Pageable pageable);

        // Find orders by date range
        @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
        Page<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        // Find orders by user and date range
        @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.createdAt BETWEEN :startDate AND :endDate")
        Page<Order> findByUserIdAndDateRange(@Param("userId") Long userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        // Count orders by user
        long countByUserId(Long userId);

        // Count orders by status
        long countByStatus(FulfillmentStatus status);

        // Count orders by payment status
        long countByPaymentStatus(PaymentStatus paymentStatus);

        // Find orders with details
        @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.id = :id")
        Optional<Order> findByIdWithDetails(@Param("id") Long id);

        // Find orders with payments
        @Query("SELECT o FROM Order o LEFT JOIN FETCH o.payments WHERE o.id = :id")
        Optional<Order> findByIdWithPayments(@Param("id") Long id);

        // Find orders with shipments
        @Query("SELECT o FROM Order o LEFT JOIN FETCH o.shipments WHERE o.id = :id")
        Optional<Order> findByIdWithShipments(@Param("id") Long id);

        // Find orders with all relationships
        @Query("SELECT o FROM Order o " +
                        "LEFT JOIN FETCH o.orderDetails " +
                        "LEFT JOIN FETCH o.payments " +
                        "LEFT JOIN FETCH o.shipments " +
                        "WHERE o.id = :id")
        Optional<Order> findByIdWithAllRelations(@Param("id") Long id);

        // Find orders with voucher
        @Query("SELECT o FROM Order o LEFT JOIN FETCH o.voucher WHERE o.id = :id")
        Optional<Order> findByIdWithVoucher(@Param("id") Long id);

        // Find orders with all relationships including voucher
        @Query("SELECT o FROM Order o " +
                        "LEFT JOIN FETCH o.orderDetails " +
                        "LEFT JOIN FETCH o.payments " +
                        "LEFT JOIN FETCH o.shipments " +
                        "LEFT JOIN FETCH o.voucher " +
                        "WHERE o.id = :id")
        Optional<Order> findByIdWithAllRelationsAndVoucher(@Param("id") Long id);

        // Find all orders with optional filters using JPQL for better type safety
        @Query("SELECT o FROM Order o WHERE " +
               "(:userId IS NULL OR o.user.id = :userId) AND " +
               "(:status IS NULL OR o.status = :status) AND " +
               "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)")
        Page<Order> findAllWithFilters(@Param("userId") Long userId,
                        @Param("status") FulfillmentStatus status,
                        @Param("paymentStatus") PaymentStatus paymentStatus,
                        Pageable pageable);

        // Get orders by date range for dashboard
        @Query(value = "SELECT o.* FROM orders o WHERE o.created_at >= :startDate AND o.created_at < :endDate", nativeQuery = true)
        List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Count orders by fulfillment status in date range
        @Query(value = "SELECT COUNT(o.*) FROM orders o WHERE o.created_at >= :date AND o.created_at < :nextDate AND o.status = :status", nativeQuery = true)
        Long countOrdersByDateAndStatus(@Param("date") LocalDateTime date, @Param("nextDate") LocalDateTime nextDate, @Param("status") String status);

        // Sum revenue by payment status in date range
        @Query(value = "SELECT COALESCE(SUM(o.total_amount), 0) FROM orders o WHERE o.created_at >= :date AND o.created_at < :nextDate AND o.payment_status = :paymentStatus", nativeQuery = true)
        BigDecimal sumRevenueByDateAndPaymentStatus(@Param("date") LocalDateTime date, @Param("nextDate") LocalDateTime nextDate, @Param("paymentStatus") String paymentStatus);

        // Count total orders by date
        @Query(value = "SELECT COUNT(o.*) FROM orders o WHERE o.created_at >= :date AND o.created_at < :nextDate", nativeQuery = true)
        Long countOrdersByDate(@Param("date") LocalDateTime date, @Param("nextDate") LocalDateTime nextDate);

        // Sum total revenue by date
        @Query(value = "SELECT COALESCE(SUM(o.total_amount), 0) FROM orders o WHERE o.created_at >= :date AND o.created_at < :nextDate", nativeQuery = true)
        BigDecimal sumTotalRevenueByDate(@Param("date") LocalDateTime date, @Param("nextDate") LocalDateTime nextDate);

        /**
         * Find order history with shipment status for size recommendation.
         * Returns individual orders with userId for similarity-weighted calculation.
         * 
         * Returns: userId, sizeLabel, shipmentStatus, orderStatus, rating, purchaseDate
         */
        @Query("""
            SELECT 
                o.user.id AS userId,
                od.sizeLabel AS sizeLabel,
                COALESCE(s.status, 'PENDING') AS shipmentStatus,
                o.status AS orderStatus,
                COALESCE(r.rating, 0) AS rating,
                o.createdAt AS purchaseDate
            FROM Order o
            JOIN o.orderDetails od
            JOIN od.productDetail pd
            LEFT JOIN o.shipments s
            LEFT JOIN Review r ON r.user.id = o.user.id AND r.productDetail.id = pd.id
            WHERE o.user.id IN :userIds
              AND pd.product.id = :productId
            ORDER BY o.createdAt DESC
            """)
        List<Object[]> findOrderHistoryWithShipmentStatus(
            @Param("userIds") List<Long> userIds,
            @Param("productId") Long productId
        );
}
