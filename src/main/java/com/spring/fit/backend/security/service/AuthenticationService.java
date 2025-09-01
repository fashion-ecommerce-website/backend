package com.spring.fit.backend.security.service;

import com.spring.fit.backend.security.domain.dto.AuthenticationRequest;
import com.spring.fit.backend.security.domain.dto.AuthenticationResponse;
import com.spring.fit.backend.security.domain.dto.RegisterRequest;
import com.spring.fit.backend.security.domain.dto.RefreshTokenRequest;

public interface AuthenticationService {
    
    AuthenticationResponse register(RegisterRequest request);
    
    AuthenticationResponse authenticate(AuthenticationRequest request);
    
    AuthenticationResponse refreshToken(RefreshTokenRequest request);
    
    void logout(String refreshToken);
}
