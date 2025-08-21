package com.spring.fit.backend.security.service;

import com.spring.fit.backend.security.domain.dto.AuthenticationRequest;
import com.spring.fit.backend.security.domain.dto.AuthenticationResponse;
import com.spring.fit.backend.security.domain.dto.RegisterRequest;
import com.spring.fit.backend.security.domain.dto.RefreshTokenRequest;

public interface AuthenticationService {
    
    /**
     * Đăng ký tài khoản mới
     * @param request Thông tin đăng ký
     * @return Thông tin authentication response
     */
    AuthenticationResponse register(RegisterRequest request);
    
    /**
     * Đăng nhập
     * @param request Thông tin đăng nhập
     * @return Thông tin authentication response
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);
    
    /**
     * Refresh token
     * @param request Refresh token request
     * @return Thông tin authentication response mới
     */
    AuthenticationResponse refreshToken(RefreshTokenRequest request);
    
    /**
     * Đăng xuất
     * @param refreshToken Refresh token để đăng xuất
     */
    void logout(String refreshToken);
}
