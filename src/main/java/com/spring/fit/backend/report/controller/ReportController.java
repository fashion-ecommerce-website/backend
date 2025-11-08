package com.spring.fit.backend.report.controller;

import com.spring.fit.backend.common.enums.PeriodType;
import com.spring.fit.backend.report.domain.dto.request.DailyReportRequest;
import com.spring.fit.backend.report.domain.dto.response.DashboardResponse;
import com.spring.fit.backend.report.service.DailyReportService;
import com.spring.fit.backend.report.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final DailyReportService dailyReportService;
    private final DashboardService dashboardService;

    @PostMapping("/daily")
    public void sendDailyReport(@Valid @RequestBody DailyReportRequest request) {
            dailyReportService.sendDailyReport(request.getEmail());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@RequestParam(defaultValue = "week") String period) {
            DashboardResponse dashboard = dashboardService.getDashboardData(PeriodType.fromString(period));
            return ResponseEntity.ok(dashboard);
    }

}