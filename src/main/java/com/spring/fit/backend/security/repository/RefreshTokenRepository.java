package com.spring.fit.backend.security.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.fit.backend.security.domain.entity.RefreshTokenEntity;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByJti(UUID jti);

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenEntity> findByUserIdAndIsRevokedFalse(Long userId);

    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.user.id = :userId AND rt.isRevoked = false AND rt.expiresAt > :now")
    List<RefreshTokenEntity> findValidTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.expiresAt < :now AND rt.isRevoked = false")
    List<RefreshTokenEntity> findExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.isRevoked = true, rt.revokedAt = :revokedAt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    void revokeAllUserTokens(@Param("userId") Long userId, @Param("revokedAt") LocalDateTime revokedAt);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.isRevoked = true, rt.revokedAt = :revokedAt WHERE rt.jti = :jti")
    void revokeTokenByJti(@Param("jti") UUID jti, @Param("revokedAt") LocalDateTime revokedAt);
}
