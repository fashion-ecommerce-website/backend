package com.spring.fit.backend.security.service.impl;

import com.spring.fit.backend.security.domain.entity.PasswordResetToken;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.PasswordResetTokenRepository;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.security.service.PasswordResetTokenService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepository;

    public PasswordResetToken createToken(UserEntity user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(ZonedDateTime.now().plusSeconds(60));
        return tokenRepository.save(token);
    }

    public boolean isValidToken(String token) {
        System.out.println(token);
        return tokenRepository.findByToken(token)
                .map(t -> t.getExpiryDate().isAfter(ZonedDateTime.now()))
                .orElse(false);
    }

    public Optional<UserEntity> getUserByToken(String token) {
        return tokenRepository.findByToken(token).map(PasswordResetToken::getUser);
    }

    @Override
    public void deleteToken(String token) {
        tokenRepository
                .delete(tokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Token not found")));
    }

    @Override
    public PasswordResetToken findByUser(UserEntity user) {
        return tokenRepository.findPasswordResetTokenByUser(user);
    }
}