package com.spring.fit.backend.report.domain.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class DailyReportRequest {
    @Email(message = "Invalid email format")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}