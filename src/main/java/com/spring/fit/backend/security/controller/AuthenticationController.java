package com.spring.fit.backend.security.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.fit.backend.security.domain.dto.AuthenticationRequest;
import com.spring.fit.backend.security.domain.dto.AuthenticationResponse;
import com.spring.fit.backend.security.domain.dto.RegisterRequest;
import com.spring.fit.backend.security.domain.dto.RefreshTokenRequest;
import com.spring.fit.backend.security.domain.dto.ChangePasswordRequest;
import com.spring.fit.backend.security.domain.dto.ResetPasswordRequest;
import com.spring.fit.backend.security.service.AuthenticationService;
import com.spring.fit.backend.security.service.OtpService;

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

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthenticationResponse response = authenticationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request) {

        AuthenticationResponse response = authenticationService.authenticate(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthenticationResponse response = authenticationService.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {

        authenticationService.logout(request.getRefreshToken());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number is required");
        }
        
        otpService.sendOtp(phone);
        return ResponseEntity.ok("OTP sent successfully");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String otpCode = request.get("otpCode");
        
        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number is required");
        }
        
        if (otpCode == null || otpCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("OTP code is required");
        }
        
        boolean isValid = otpService.verifyOtp(phone, otpCode);
        
        if (isValid) {
            // Update user's phone_verified status
            authenticationService.verifyPhone(phone);
            return ResponseEntity.ok("Phone verified successfully");
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
    public ResponseEntity<Void> forgotPassword(@RequestBody String email) {
        authenticationService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}
