package com.spring.fit.backend.security.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.security.domain.dto.AuthenticationRequest;
import com.spring.fit.backend.security.domain.dto.AuthenticationResponse;
import com.spring.fit.backend.security.domain.dto.RegisterRequest;
import com.spring.fit.backend.security.domain.dto.RefreshTokenRequest;
import com.spring.fit.backend.security.domain.dto.ChangePasswordRequest;
import com.spring.fit.backend.security.domain.dto.ResetPasswordRequest;
import com.spring.fit.backend.security.domain.dto.GoogleLoginRequest;
import com.spring.fit.backend.security.service.AuthCookieService;
import com.spring.fit.backend.security.service.AuthenticationService;
import com.spring.fit.backend.security.service.OtpService;
import com.spring.fit.backend.security.service.RateLimiterService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final OtpService otpService;
    private final AuthCookieService authCookieService;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthenticationResponse response = authenticationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletResponse servletResponse) {

        AuthenticationResponse response = authenticationService.authenticate(request);
        authCookieService.setRefreshTokenCookie(servletResponse, response.getRefreshToken());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshTokenRequest requestBody,
            HttpServletResponse servletResponse) {

        String tokenToUse = authCookieService.resolveRefreshToken(
                cookieRefreshToken,
                requestBody != null ? requestBody.getRefreshToken() : null);

        if (tokenToUse == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Refresh token is required in cookie or body");
        }

        AuthenticationResponse response = authenticationService.refreshToken(new RefreshTokenRequest(tokenToUse));
        authCookieService.setRefreshTokenCookie(servletResponse, response.getRefreshToken());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshTokenRequest requestBody,
            HttpServletResponse servletResponse) {

        String tokenToUse = authCookieService.resolveRefreshToken(
                cookieRefreshToken,
                requestBody != null ? requestBody.getRefreshToken() : null);

        if (tokenToUse != null) {
            authenticationService.logout(tokenToUse);
        }

        authCookieService.clearRefreshTokenCookie(servletResponse);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        
        if (!rateLimiterService.allowOtpRequest(email)) {
            long waitSeconds = rateLimiterService.getOtpCooldownRemainingSeconds(email);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Please wait " + waitSeconds + " seconds before requesting another OTP.");
        }

        otpService.sendOtp(email);
        return ResponseEntity.ok("OTP sent successfully to email");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otpCode = request.get("otpCode");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        
        if (otpCode == null || otpCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("OTP code is required");
        }
        
        boolean isValid = otpService.verifyOtp(email, otpCode);
        
        if (isValid) {
            // Update user's email_verified status
            authenticationService.verifyEmail(email);
            return ResponseEntity.ok("Email verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody Object body) {
        String email = null;
        if (body instanceof Map<?, ?> map) {
            Object val = map.get("email");
            if (val != null) email = val.toString();
        } else if (body instanceof String str) {
            email = str;
        }

        if (email != null) {
            email = email.replace("\"", "").trim();
        }

        authenticationService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/google-login")
    public ResponseEntity<AuthenticationResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletResponse servletResponse) {
        
        try {
            AuthenticationResponse response = authenticationService.googleLogin(request);
            authCookieService.setRefreshTokenCookie(servletResponse, response.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Google login failed error: ", e);
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Google login failed: " + e.getMessage());
        }
    }
}
