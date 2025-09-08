package com.spring.fit.backend.security.service;

import com.spring.fit.backend.security.domain.entity.PasswordResetToken;
import com.spring.fit.backend.security.domain.entity.UserEntity;

import java.util.Optional;

public interface PasswordResetTokenService {
    PasswordResetToken createToken(UserEntity user);

    boolean isValidToken(String token);

    Optional<UserEntity> getUserByToken(String token);

    PasswordResetToken findByUser(UserEntity user);

    void deleteToken(String token);
}
