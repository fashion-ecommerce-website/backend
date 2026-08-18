package com.spring.fit.backend.security.service.impl;

import com.spring.fit.backend.security.domain.entity.PasswordResetToken;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.PasswordResetTokenRepository;
import com.spring.fit.backend.security.service.PasswordResetTokenService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepository;

    @Transactional
    public PasswordResetToken createToken(UserEntity user) {
        // Delete any existing token for the user first (flush immediately to avoid duplicate key)
        tokenRepository.deleteByUser(user);
        tokenRepository.flush();

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(ZonedDateTime.now().plusMinutes(15));
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
    @Transactional
    public void deleteToken(String token) {
        tokenRepository.deleteByToken(token);
    }

    @Override
    public PasswordResetToken findByUser(UserEntity user) {
        return tokenRepository.findPasswordResetTokenByUser(user);
    }
}