package com.spring.fit.backend.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtConfig {
    private String secret;
    private long expiration; // Access token expiration time in milliseconds
    private long refreshExpiration; // Refresh token expiration time in milliseconds
}
