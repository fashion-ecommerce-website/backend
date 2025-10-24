package com.spring.fit.backend.report.controller;

import com.spring.fit.backend.report.service.DailyReportService;
import com.spring.fit.backend.report.service.PdfReportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final DailyReportService dailyReportService;

    @PostMapping("/daily")
    public ResponseEntity<String> sendDailyReport(@Valid @RequestBody DailyReportRequest request) {
        log.info("Received request to send daily report to: {}", request.getEmail());
        
        try {
            dailyReportService.sendDailyReport(request.getEmail());
            return ResponseEntity.ok("Daily report sent successfully to " + request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send daily report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send daily report: " + e.getMessage());
        }
    }

    public static class DailyReportRequest {
        @Email(message = "Invalid email format")
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}