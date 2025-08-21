package com.spring.fit.backend.security.controller;

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
import com.spring.fit.backend.security.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Đăng ký tài khoản mới
     * 
     * @param request Thông tin đăng ký
     * @return Thông tin authentication response
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        
        AuthenticationResponse response = authenticationService.register(request);
        
        log.info("Registration successful for email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Đăng nhập
     * 
     * @param request Thông tin đăng nhập
     * @return Thông tin authentication response
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        
        AuthenticationResponse response = authenticationService.authenticate(request);
        
        log.info("Login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh token
     * 
     * @param request Refresh token request
     * @return Thông tin authentication response mới
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received refresh token request");
        
        AuthenticationResponse response = authenticationService.refreshToken(request);
        
        log.info("Token refresh successful");
        return ResponseEntity.ok(response);
    }

    /**
     * Đăng xuất
     * 
     * @param request Refresh token request
     * @return Response trống
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received logout request");
        
        authenticationService.logout(request.getRefreshToken());
        
        log.info("Logout successful");
        return ResponseEntity.ok().build();
    }
}
