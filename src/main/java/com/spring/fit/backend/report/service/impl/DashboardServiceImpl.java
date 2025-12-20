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
import java.util.ArrayList;
import java.util.List;
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

        // Only support MONTH and YEAR
        if (period != PeriodType.MONTH && period != PeriodType.YEAR) {
            log.warn("Unsupported period type: {}, defaulting to MONTH", period);
            period = PeriodType.MONTH;
        }

        // Get summary data
        DashboardResponse.SummaryDto summary = getSummary();

        // Get chart data based on period
        List<DashboardResponse.ChartDataDto> chartData = switch (period) {
            case MONTH -> getChartDataByMonth();
            case YEAR -> getChartDataByYear();
            default -> getChartDataByMonth(); // fallback
        };

        return DashboardResponse.builder()
                .period(period.name())
                .summary(summary)
                .chartData(chartData)
                .build();
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

    private List<DashboardResponse.ChartDataDto> getChartDataByMonth() {
        List<DashboardResponse.ChartDataDto> chartDataList = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        
        // Get data for 12 months of current year (1-12)
        for (int month = 1; month <= 12; month++) {
            // Count orders by status
            Long totalOrders = orderRepository.countOrdersByMonthAndYear(currentYear, month);
            if (totalOrders == null) totalOrders = 0L;
            
            Long completedOrders = orderRepository.countOrdersByMonthYearAndStatus(currentYear, month, FulfillmentStatus.FULFILLED.getValue());
            if (completedOrders == null) completedOrders = 0L;
            
            Long cancelledOrders = orderRepository.countOrdersByMonthYearAndStatus(currentYear, month, FulfillmentStatus.CANCELLED.getValue());
            if (cancelledOrders == null) cancelledOrders = 0L;
            
            Long unfulfilledOrders = orderRepository.countOrdersByMonthYearAndStatus(currentYear, month, FulfillmentStatus.UNFULFILLED.getValue());
            if (unfulfilledOrders == null) unfulfilledOrders = 0L;
            
            Long partiallyFulfilledOrders = orderRepository.countOrdersByMonthYearAndStatus(currentYear, month, FulfillmentStatus.PARTIALLY_FULFILLED.getValue());
            if (partiallyFulfilledOrders == null) partiallyFulfilledOrders = 0L;
            
            Long pendingOrders = unfulfilledOrders + partiallyFulfilledOrders;

            // Calculate revenue
            BigDecimal totalRevenue = orderRepository.sumTotalRevenueByMonthAndYear(currentYear, month);
            if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
            
            BigDecimal paidRevenue = orderRepository.sumRevenueByMonthYearAndPaymentStatus(currentYear, month, PaymentStatus.PAID.getValue());
            if (paidRevenue == null) paidRevenue = BigDecimal.ZERO;
            
            BigDecimal unpaidAmount = orderRepository.sumRevenueByMonthYearAndPaymentStatus(currentYear, month, PaymentStatus.UNPAID.getValue());
            if (unpaidAmount == null) unpaidAmount = BigDecimal.ZERO;
            
            BigDecimal partiallyPaidAmount = orderRepository.sumRevenueByMonthYearAndPaymentStatus(currentYear, month, PaymentStatus.PARTIALLY_PAID.getValue());
            if (partiallyPaidAmount == null) partiallyPaidAmount = BigDecimal.ZERO;
            
            BigDecimal unpaidRevenue = unpaidAmount.add(partiallyPaidAmount);
            
            BigDecimal refundedRevenue = orderRepository.sumRevenueByMonthYearAndPaymentStatus(currentYear, month, PaymentStatus.REFUNDED.getValue());
            if (refundedRevenue == null) refundedRevenue = BigDecimal.ZERO;

            DashboardResponse.ChartDataDto chartData = DashboardResponse.ChartDataDto.builder()
                    .target(String.valueOf(month))
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
        }

        return chartDataList;
    }

    private List<DashboardResponse.ChartDataDto> getChartDataByYear() {
        List<DashboardResponse.ChartDataDto> chartDataList = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        
        // Get data for 12 years from current year going back (2025, 2024, ..., 2014)
        for (int i = 0; i < 12; i++) {
            int year = currentYear - i;
            
            // Count orders by status
            Long totalOrders = orderRepository.countOrdersByYear(year);
            if (totalOrders == null) totalOrders = 0L;
            
            Long completedOrders = orderRepository.countOrdersByYearAndStatus(year, FulfillmentStatus.FULFILLED.getValue());
            if (completedOrders == null) completedOrders = 0L;
            
            Long cancelledOrders = orderRepository.countOrdersByYearAndStatus(year, FulfillmentStatus.CANCELLED.getValue());
            if (cancelledOrders == null) cancelledOrders = 0L;
            
            Long unfulfilledOrders = orderRepository.countOrdersByYearAndStatus(year, FulfillmentStatus.UNFULFILLED.getValue());
            if (unfulfilledOrders == null) unfulfilledOrders = 0L;
            
            Long partiallyFulfilledOrders = orderRepository.countOrdersByYearAndStatus(year, FulfillmentStatus.PARTIALLY_FULFILLED.getValue());
            if (partiallyFulfilledOrders == null) partiallyFulfilledOrders = 0L;
            
            Long pendingOrders = unfulfilledOrders + partiallyFulfilledOrders;

            // Calculate revenue
            BigDecimal totalRevenue = orderRepository.sumTotalRevenueByYear(year);
            if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
            
            BigDecimal paidRevenue = orderRepository.sumRevenueByYearAndPaymentStatus(year, PaymentStatus.PAID.getValue());
            if (paidRevenue == null) paidRevenue = BigDecimal.ZERO;
            
            BigDecimal unpaidAmount = orderRepository.sumRevenueByYearAndPaymentStatus(year, PaymentStatus.UNPAID.getValue());
            if (unpaidAmount == null) unpaidAmount = BigDecimal.ZERO;
            
            BigDecimal partiallyPaidAmount = orderRepository.sumRevenueByYearAndPaymentStatus(year, PaymentStatus.PARTIALLY_PAID.getValue());
            if (partiallyPaidAmount == null) partiallyPaidAmount = BigDecimal.ZERO;
            
            BigDecimal unpaidRevenue = unpaidAmount.add(partiallyPaidAmount);
            
            BigDecimal refundedRevenue = orderRepository.sumRevenueByYearAndPaymentStatus(year, PaymentStatus.REFUNDED.getValue());
            if (refundedRevenue == null) refundedRevenue = BigDecimal.ZERO;

            DashboardResponse.ChartDataDto chartData = DashboardResponse.ChartDataDto.builder()
                    .target(String.valueOf(year))
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
        }

        return chartDataList;
    }
}

