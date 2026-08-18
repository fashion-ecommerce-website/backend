package com.spring.fit.backend.security.repository;

import com.spring.fit.backend.security.domain.entity.PasswordResetToken;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    PasswordResetToken findPasswordResetTokenByUser(UserEntity user);

    @Modifying
    void deleteByUser(UserEntity user);

    @Modifying
    void deleteByToken(String token);
}
