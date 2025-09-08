package com.spring.fit.backend.security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.fit.backend.security.domain.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.isActive = true")
    Optional<UserEntity> findActiveUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.isActive = true")
    Optional<UserEntity> findActiveUserByUsername(@Param("username") String username);

    Optional<UserEntity> findByResetPasswordToken(String resetPasswordToken);
}
