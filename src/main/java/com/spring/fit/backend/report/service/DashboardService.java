package com.spring.fit.backend.report.service;

import com.spring.fit.backend.common.enums.PeriodType;
import com.spring.fit.backend.report.domain.dto.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboardData(PeriodType period);
}

