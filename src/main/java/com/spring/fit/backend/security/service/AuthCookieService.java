package com.spring.fit.backend.security.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthCookieService {

    private static final String COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/api/auth";
    private static final long MAX_AGE_SECONDS = 7 * 24 * 60 * 60; // 7 days in seconds

    /**
     * Injects HttpOnly RefreshToken cookie into HTTP response headers
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return;
        }

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false) // Set to true when serving HTTPS in production
                .path(COOKIE_PATH)
                .maxAge(MAX_AGE_SECONDS)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Clears HttpOnly RefreshToken cookie from HTTP response headers
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path(COOKIE_PATH)
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Resolves refresh token string from Cookie or fallback JSON request body
     */
    public String resolveRefreshToken(String cookieToken, String bodyToken) {
        if (cookieToken != null && !cookieToken.trim().isEmpty()) {
            return cookieToken.trim();
        }
        if (bodyToken != null && !bodyToken.trim().isEmpty()) {
            return bodyToken.trim();
        }
        return null;
    }
}
