package com.spring.fit.backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String type;
    private String message;
    
    public AuthResponse(String token, String type) {
        this.token = token;
        this.type = type;
    }
} 