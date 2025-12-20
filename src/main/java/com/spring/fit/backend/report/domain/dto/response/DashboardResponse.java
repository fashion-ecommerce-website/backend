package com.spring.fit.backend.report.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private String period;
    private SummaryDto summary;
    private List<ChartDataDto> chartData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        private Long totalUsers;
        private Long totalProducts;
        private Long totalOrders;
        private BigDecimal totalRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDataDto {
        private String target;  // month (1-12) or year (2025, 2024, ...)
        private Long totalOrders;
        private Long completedOrders;
        private Long cancelledOrders;
        private Long pendingOrders;
        private BigDecimal totalRevenue;
        private BigDecimal unpaidRevenue;
        private BigDecimal paidRevenue;
        private BigDecimal refundedRevenue;
    }
}

