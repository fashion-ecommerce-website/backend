package com.spring.fit.backend.report.service.impl;

import com.spring.fit.backend.common.enums.FulfillmentStatus;
import com.spring.fit.backend.common.enums.PaymentStatus;
import com.spring.fit.backend.common.enums.PeriodType;
import com.spring.fit.backend.order.repository.OrderRepository;
import com.spring.fit.backend.product.repository.ProductMainRepository;
import com.spring.fit.backend.report.domain.dto.response.DashboardResponse;
import com.spring.fit.backend.report.service.DashboardService;
import com.spring.fit.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.spring.fit.backend.common.util.DateUtils. *;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductMainRepository productMainRepository;

    @Override
    public DashboardResponse getDashboardData(PeriodType period) {
        log.info("Getting dashboard data for period: {}", period);

        // Calculate date range based on period
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(period, endDate);

        // Get summary data
        DashboardResponse.SummaryDto summary = getSummary();

        // Get chart data
        List<DashboardResponse.ChartDataDto> chartData = getChartData(startDate, endDate);

        return DashboardResponse.builder()
                .period(period.getValue())
                .summary(summary)
                .chartData(chartData)
                .build();
    }

    private LocalDate calculateStartDate(PeriodType period, LocalDate endDate) {
        return switch (period) {
            case DAY -> endDate;
            case WEEK -> endDate.minusDays(6);
            case MONTH -> endDate.minusDays(29);
            case YEAR -> endDate.minusDays(364);
        };
    }

    private DashboardResponse.SummaryDto getSummary() {
        long totalUsers = userRepository.count();
        long totalProducts = productMainRepository.count();
        long totalOrders = orderRepository.count();
        
        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardResponse.SummaryDto.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .build();
    }

    private List<DashboardResponse.ChartDataDto> getChartData(LocalDate startDate, LocalDate endDate) {
        List<DashboardResponse.ChartDataDto> chartDataList = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDateTime dateTime = currentDate.atStartOfDay();
            LocalDateTime nextDate = currentDate.plusDays(1).atStartOfDay();
            
            // Count orders by status
            Long totalOrders = orderRepository.countOrdersByDate(dateTime, nextDate);
            if (totalOrders == null) totalOrders = 0L;
            
            Long completedOrders = orderRepository.countOrdersByDateAndStatus(dateTime, nextDate, FulfillmentStatus.FULFILLED.getValue());
            if (completedOrders == null) completedOrders = 0L;
            
            Long cancelledOrders = orderRepository.countOrdersByDateAndStatus(dateTime, nextDate, FulfillmentStatus.CANCELLED.getValue());
            if (cancelledOrders == null) cancelledOrders = 0L;
            
            Long unfulfilledOrders = orderRepository.countOrdersByDateAndStatus(dateTime, nextDate, FulfillmentStatus.UNFULFILLED.getValue());
            if (unfulfilledOrders == null) unfulfilledOrders = 0L;
            
            Long partiallyFulfilledOrders = orderRepository.countOrdersByDateAndStatus(dateTime, nextDate, FulfillmentStatus.PARTIALLY_FULFILLED.getValue());
            if (partiallyFulfilledOrders == null) partiallyFulfilledOrders = 0L;
            
            Long pendingOrders = unfulfilledOrders + partiallyFulfilledOrders;

            // Calculate revenue
            BigDecimal totalRevenue = orderRepository.sumTotalRevenueByDate(dateTime, nextDate);
            if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
            
            BigDecimal paidRevenue = orderRepository.sumRevenueByDateAndPaymentStatus(dateTime, nextDate, PaymentStatus.PAID.getValue());
            if (paidRevenue == null) paidRevenue = BigDecimal.ZERO;
            
            BigDecimal unpaidAmount = orderRepository.sumRevenueByDateAndPaymentStatus(dateTime, nextDate, PaymentStatus.UNPAID.getValue());
            if (unpaidAmount == null) unpaidAmount = BigDecimal.ZERO;
            
            BigDecimal partiallyPaidAmount = orderRepository.sumRevenueByDateAndPaymentStatus(dateTime, nextDate, PaymentStatus.PARTIALLY_PAID.getValue());
            if (partiallyPaidAmount == null) partiallyPaidAmount = BigDecimal.ZERO;
            
            BigDecimal unpaidRevenue = unpaidAmount.add(partiallyPaidAmount);
            
            BigDecimal refundedRevenue = orderRepository.sumRevenueByDateAndPaymentStatus(dateTime, nextDate, PaymentStatus.REFUNDED.getValue());
            if (refundedRevenue == null) refundedRevenue = BigDecimal.ZERO;

            // Get day label (Mon, Tue, etc.)
            String label = currentDate.format(DAY_LABEL_FORMATTER);

            DashboardResponse.ChartDataDto chartData = DashboardResponse.ChartDataDto.builder()
                    .date(currentDate.format(DATE_FORMATTER))
                    .label(label)
                    .totalOrders(totalOrders)
                    .completedOrders(completedOrders)
                    .cancelledOrders(cancelledOrders)
                    .pendingOrders(pendingOrders)
                    .totalRevenue(totalRevenue)
                    .unpaidRevenue(unpaidRevenue)
                    .paidRevenue(paidRevenue)
                    .refundedRevenue(refundedRevenue)
                    .build();

            chartDataList.add(chartData);
            currentDate = currentDate.plusDays(1);
        }

        return chartDataList;
    }
}

