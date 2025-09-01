package com.spring.fit.backend.security.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.security.domain.dto.AuthenticationRequest;
import com.spring.fit.backend.security.domain.dto.AuthenticationResponse;
import com.spring.fit.backend.security.domain.dto.RegisterRequest;
import com.spring.fit.backend.security.domain.dto.RefreshTokenRequest;
import com.spring.fit.backend.security.domain.entity.RefreshTokenEntity;
import com.spring.fit.backend.security.domain.entity.RoleEntity;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.domain.entity.UserRoleEntity;
import com.spring.fit.backend.security.jwt.JwtService;
import com.spring.fit.backend.security.repository.RefreshTokenRepository;
import com.spring.fit.backend.security.repository.RoleRepository;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.security.service.AuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final RefreshTokenRepository refreshTokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        @Override
        @Transactional
        public AuthenticationResponse register(RegisterRequest request) {
                log.info("Registering new user with email: {}", request.getEmail());

                // Check if user already exists
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new ErrorException(HttpStatus.CONFLICT, "Email already registered");
                }

                if (userRepository.existsByUsername(request.getUsername())) {
                        throw new ErrorException(HttpStatus.CONFLICT, "Username already taken");
                }

                // Create new user
                UserEntity user = UserEntity.builder()
                                .email(request.getEmail())
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .phone(request.getPhone())
                                .isActive(true)
                                .emailVerified(false)
                                .phoneVerified(false)
                                .build();

                // Ensure collections are initialized
                if (user.getUserRoles() == null) {
                        user.setUserRoles(new LinkedHashSet<>());
                }
                if (user.getRefreshTokens() == null) {
                        user.setRefreshTokens(new LinkedHashSet<>());
                }

                user = userRepository.save(user);

                // Assign default USER role
                RoleEntity userRole = roleRepository.findByRoleName("USER")
                                .orElseThrow(() -> new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Default USER role not found"));

                UserRoleEntity userRoleEntity = UserRoleEntity.builder()
                                .user(user)
                                .role(userRole)
                                .isActive(true)
                                .build();

                user.getUserRoles().add(userRoleEntity);
                userRepository.save(user);

                // Generate tokens
                UserDetails userDetails = createUserDetails(user);
                String accessToken = jwtService.generateToken(userDetails);
                String refreshToken = generateRefreshToken(userDetails);

                log.info("User registered successfully: {}", user.getEmail());

                return AuthenticationResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .expiresIn(86400000L) // 24 hours
                                .build();
        }

        @Override
        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                log.info("Authenticating user with email: {}", request.getEmail());

                // Authenticate user
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                // Get user details
                UserEntity user = userRepository.findActiveUserByEmail(request.getEmail())
                                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

                // Update last login
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);

                // Generate tokens
                UserDetails userDetails = createUserDetails(user);
                String accessToken = jwtService.generateToken(userDetails);
                String refreshToken = generateRefreshToken(userDetails);

                log.info("User authenticated successfully: {}", user.getEmail());

                return AuthenticationResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .expiresIn(86400000L) // 24 hours
                                .build();
        }

        @Override
        @Transactional
        public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
                log.info("Refreshing token");

                // Find refresh token
                RefreshTokenEntity refreshToken = refreshTokenRepository.findByTokenHash(request.getRefreshToken())
                                .orElseThrow(() -> new ErrorException(HttpStatus.UNAUTHORIZED,
                                                "Invalid refresh token"));

                // Check if token is revoked or expired
                if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                        throw new ErrorException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
                }

                // Get user
                UserEntity user = refreshToken.getUser();

                // Generate new tokens
                UserDetails userDetails = createUserDetails(user);
                String newAccessToken = jwtService.generateToken(userDetails);
                String newRefreshToken = generateRefreshToken(userDetails);

                // Revoke old refresh token
                refreshToken.setRevoked(true);
                refreshToken.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(refreshToken);

                log.info("Token refreshed successfully for user: {}", user.getEmail());

                return AuthenticationResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(newRefreshToken)
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .expiresIn(86400000L) // 24 hours
                                .build();
        }

        @Override
        @Transactional
        public void logout(String refreshToken) {
                log.info("Logging out user");

                RefreshTokenEntity token = refreshTokenRepository.findByTokenHash(refreshToken)
                                .orElse(null);

                if (token != null) {
                        token.setRevoked(true);
                        token.setRevokedAt(LocalDateTime.now());
                        refreshTokenRepository.save(token);
                        log.info("User logged out successfully: {}", token.getUser().getEmail());
                }
        }

        private UserDetails createUserDetails(UserEntity user) {
                // Create authorities safely
                List<SimpleGrantedAuthority> authorities = createAuthorities(user);
                
                return org.springframework.security.core.userdetails.User.builder()
                                .username(user.getEmail())
                                .password(user.getPassword())
                                .authorities(authorities)
                                .accountExpired(!user.isActive())
                                .accountLocked(false)
                                .credentialsExpired(false)
                                .disabled(!user.isActive())
                                .build();
        }
        
        /**
         * Create authorities safely from user roles
         */
        private List<SimpleGrantedAuthority> createAuthorities(UserEntity user) {
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                
                try {
                        if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
                                authorities = user.getUserRoles().stream()
                                                .filter(userRole -> userRole != null && userRole.isActive())
                                                .filter(userRole -> userRole.getRole() != null && userRole.getRole().isActive())
                                                .map(userRole -> {
                                                        String roleName = userRole.getRole().getRoleName();
                                                        if (roleName != null && !roleName.trim().isEmpty()) {
                                                                return new SimpleGrantedAuthority("ROLE_" + roleName.trim());
                                                        }
                                                        return null;
                                                })
                                                .filter(authority -> authority != null)
                                                .collect(Collectors.toList());
                        }
                        
                        // Add default USER role if no roles found
                        if (authorities.isEmpty()) {
                                log.warn("No valid roles found for user {}, adding default USER role", user.getEmail());
                                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                        }
                        
                } catch (Exception e) {
                        log.error("Error creating authorities for user {}: {}", user.getEmail(), e.getMessage(), e);
                        // Add default USER role as fallback
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }
                
                return authorities;
        }

        private String generateRefreshToken(UserDetails userDetails) {
                UserEntity user = userRepository.findActiveUserByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

                UUID jti = UUID.randomUUID();
                String refreshToken = jwtService.generateRefreshToken(userDetails);

                RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                                .user(user)
                                .jti(jti)
                                .tokenHash(refreshToken)
                                .expiresAt(LocalDateTime.now().plusDays(7)) // 7 days
                                .isRevoked(false)
                                .build();

                refreshTokenRepository.save(refreshTokenEntity);

                return refreshToken;
        }
}
