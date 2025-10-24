package com.spring.fit.backend.report.controller;

import com.spring.fit.backend.report.service.DailyReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DailyReportService dailyReportService;

    @Test
    void sendDailyReport_ValidEmail_ShouldReturnSuccess() throws Exception {
        // Given
        String email = "test@example.com";
        doNothing().when(dailyReportService).sendDailyReport(anyString());

        // When & Then
        mockMvc.perform(post("/api/reports/daily")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Daily report sent successfully to " + email));
    }

    @Test
    void sendDailyReport_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/reports/daily")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"invalid-email\"}"))
                .andExpect(status().isBadRequest());
    }
}
