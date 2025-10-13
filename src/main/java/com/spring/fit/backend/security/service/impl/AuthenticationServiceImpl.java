package com.spring.fit.backend.security.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.security.domain.dto.AuthenticationRequest;
import com.spring.fit.backend.security.domain.dto.AuthenticationResponse;
import com.spring.fit.backend.security.domain.dto.RegisterRequest;
import com.spring.fit.backend.security.domain.dto.ChangePasswordRequest;
import com.spring.fit.backend.security.domain.dto.ResetPasswordRequest;
import com.spring.fit.backend.security.domain.dto.RefreshTokenRequest;
import com.spring.fit.backend.security.domain.dto.GoogleLoginRequest;
import com.spring.fit.backend.security.domain.entity.PasswordResetToken;
import com.spring.fit.backend.security.domain.entity.RefreshTokenEntity;
import com.spring.fit.backend.security.domain.entity.RoleEntity;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.domain.entity.UserRoleEntity;
import com.spring.fit.backend.security.jwt.JwtService;
import com.spring.fit.backend.security.repository.RefreshTokenRepository;
import com.spring.fit.backend.security.repository.RoleRepository;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.security.service.AuthenticationService;
import com.spring.fit.backend.security.service.OtpService;
import com.spring.fit.backend.security.service.EmailService;
import com.spring.fit.backend.security.service.PasswordResetTokenService;
import com.spring.fit.backend.security.service.FirebaseService;

import jakarta.mail.MessagingException;
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
        private final OtpService otpService;
        private final PasswordResetTokenService tokenService;
        private final EmailService emailService;
        private final FirebaseService firebaseService;

        @Override
        @Transactional
        public AuthenticationResponse register(RegisterRequest request) {
                log.info("Inside AuthenticationServiceImpl.register email={}", request.getEmail());

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

                // Send OTP to email
                try {
                        otpService.sendOtp(request.getEmail());
                        log.info("Inside AuthenticationServiceImpl.register otpSent email={}", request.getEmail());
                } catch (Exception e) {
                        log.error("Failed to send OTP to email {}: {}", request.getEmail(), e.getMessage());
                }

                // Generate tokens
                UserDetails userDetails = createUserDetails(user);
                String accessToken = jwtService.generateToken(userDetails);
                String refreshToken = generateRefreshToken(userDetails);

                log.info("Inside AuthenticationServiceImpl.register success email={}", user.getEmail());

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
                log.info("Inside AuthenticationServiceImpl.authenticate email={}", request.getEmail());

                // Authenticate user
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                // Get user details
                UserEntity user = userRepository.findActiveUserByEmail(request.getEmail())
                                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

                // Check if email is verified
                if (!user.isEmailVerified()) {
                        throw new ErrorException(HttpStatus.FORBIDDEN, "Email must be verified before login");
                }

                // Update last login
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);

                // Generate tokens
                UserDetails userDetails = createUserDetails(user);
                String accessToken = jwtService.generateToken(userDetails);
                String refreshToken = generateRefreshToken(userDetails);

                log.info("Inside AuthenticationServiceImpl.authenticate success email={}", user.getEmail());

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
                log.info("Inside AuthenticationServiceImpl.refreshToken");

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

                log.info("Inside AuthenticationServiceImpl.refreshToken success email={}", user.getEmail());

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
                log.info("Inside AuthenticationServiceImpl.logout");

                RefreshTokenEntity token = refreshTokenRepository.findByTokenHash(refreshToken)
                                .orElse(null);

                if (token != null) {
                        token.setRevoked(true);
                        token.setRevokedAt(LocalDateTime.now());
                        refreshTokenRepository.save(token);
                        log.info("Inside AuthenticationServiceImpl.logout success");
                }
        }

        @Override
        @Transactional
        public void changePassword(ChangePasswordRequest request) {
                // Get email from SecurityContext (JWT token)
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                log.info("Inside AuthenticationServiceImpl.changePassword email={}", email);

                UserEntity user = userRepository.findActiveUserByEmail(email)
                                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        throw new ErrorException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                log.info("Inside AuthenticationServiceImpl.changePassword success email={}", email);
        }

        @Override
        @Transactional
        public void resetPassword(ResetPasswordRequest request) {
                log.info("Inside AuthenticationServiceImpl.resetPassword");

                UserEntity user = userRepository.findByResetPasswordToken(request.getToken())
                                .orElseThrow(() -> new ErrorException(HttpStatus.BAD_REQUEST, "Invalid reset token"));

                if (user.getResetPasswordTokenExpiresAt() == null
                                || user.getResetPasswordTokenExpiresAt().isBefore(LocalDateTime.now())) {
                        throw new ErrorException(HttpStatus.BAD_REQUEST, "Reset token expired");
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                user.setResetPasswordToken(null);
                user.setResetPasswordTokenExpiresAt(null);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                log.info("Inside AuthenticationServiceImpl.resetPassword success email={}", user.getEmail());
        }

        @Override
        @Transactional
        public void forgotPassword(String email) {
                Optional<UserEntity> user = userRepository.findByEmail(email);
                if (user.isPresent()) {
                        PasswordResetToken tokenIxisted = tokenService.findByUser(user.get());
                        if (tokenIxisted != null) {
                                tokenService.deleteToken(tokenIxisted.getToken());
                        }
                        PasswordResetToken token = tokenService.createToken(user.get());
                        String resetLink = "http://localhost:3000/resetPassword?token=" + token.getToken();
                        try {
                                emailService.sendEmail(email, "Password Reset",
                                                "Click here to reset your password: " + resetLink + "\n "
                                                                + "Reset link is valid for 60 seconds");
                        } catch (MessagingException e) {
                                throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email");
                        }
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

                // Simply add default USER role to avoid LazyInitializationException
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                
                log.info("Inside AuthenticationServiceImpl.createAuthorities email={}, authorities={}", user.getEmail(), authorities);

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

        @Override
        @Transactional
        public void verifyEmail(String email) {
                log.info("Inside AuthenticationServiceImpl.verifyEmail email={}", email);
                
                UserEntity user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
                
                user.setEmailVerified(true);
                userRepository.save(user);
                
                log.info("Inside AuthenticationServiceImpl.verifyEmail success email={}", user.getEmail());
        }

        @Override
        @Transactional
        public AuthenticationResponse googleLogin(GoogleLoginRequest request) {
                log.info("Inside AuthenticationServiceImpl.googleLogin email={}", request.getEmail());
                
                UserEntity user;
                
                // Check if user exists
                Optional<UserEntity> existingUser = userRepository.findByEmail(request.getEmail());
                
                if (existingUser.isPresent()) {
                        user = existingUser.get();
                        log.info("Existing Google user found: {}", user.getEmail());
                        
                        // Update user info from Google
                        user.setUsername(request.getName() != null ? 
                                request.getName().replaceAll("\\s+", "").toLowerCase() : 
                                request.getEmail().split("@")[0]);
                        
                        if (request.getPicture() != null) {
                                user.setAvatarUrl(request.getPicture());
                        }
                        
                        user.setEmailVerified(true); 
                        user.setLastLoginAt(LocalDateTime.now());
                        user.setUpdatedAt(LocalDateTime.now());
                        
                } else {
                        // Create new user
                        log.info("Creating new Google user: {}", request.getEmail());
                        
                        user = UserEntity.builder()
                                .email(request.getEmail())
                                .username(request.getName() != null ? 
                                        request.getName().replaceAll("\\s+", "").toLowerCase() : 
                                        request.getEmail().split("@")[0])
                                .password("") 
                                .avatarUrl(request.getPicture())
                                .isActive(true)
                                .emailVerified(true) 
                                .phoneVerified(false)
                                .lastLoginAt(LocalDateTime.now())
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
                        user = userRepository.save(user);
                }
                
                // Generate JWT tokens
                UserDetails userDetails = createUserDetails(user);
                String accessToken = jwtService.generateToken(userDetails);
                String refreshToken = generateRefreshToken(userDetails);
                
                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .tokenType(null)
                        .expiresIn(86400000L)
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build();
        }
}
