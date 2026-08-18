package com.spring.fit.backend.security.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.spring.fit.backend.user.domain.entity.UserRank;
import com.spring.fit.backend.user.repository.UserRankRepository;
import com.spring.fit.backend.user.domain.enums.RankThreshold;
import com.spring.fit.backend.security.domain.enums.RoleType;
import com.spring.fit.backend.security.service.RateLimiterService;
import com.spring.fit.backend.security.util.TokenHashUtils;
import org.springframework.beans.factory.annotation.Value;
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
        private final UserRankRepository userRankRepository;
        private final FirebaseService firebaseService;
        private final RateLimiterService rateLimiterService;

        @Value("${app.frontend.url:http://localhost:3000}")
        private String frontendUrl;

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
                RoleEntity userRole = roleRepository.findByRoleName(RoleType.USER.getRoleName())
                                .orElseThrow(() -> new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Default USER role not found"));

                UserRoleEntity userRoleEntity = UserRoleEntity.builder()
                                .user(user)
                                .role(userRole)
                                .isActive(true)
                                .build();

                user.getUserRoles().add(userRoleEntity);

                UserRank userRank = userRankRepository.findByCode(RankThreshold.BRONZE.name())
                        .orElseThrow(() -> new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Default USER rank not found"));

                user.setRankId(userRank.getId());

                userRepository.save(user);

                // Send OTP to email
                try {
                        otpService.sendOtp(request.getEmail());
                        log.info("Inside AuthenticationServiceImpl.register otpSent email={}", request.getEmail());
                } catch (Exception e) {
                        log.error("Failed to send OTP to email {}: {}", request.getEmail(), e.getMessage());
                }

                log.info("Inside AuthenticationServiceImpl.register success email={}. Requiring OTP verification.", user.getEmail());

                return AuthenticationResponse.builder()
                                .accessToken(null)
                                .refreshToken(null)
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .expiresIn(0L)
                                .build();
        }

        @Override
        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                log.info("Inside AuthenticationServiceImpl.authenticate email={}", request.getEmail());

                log.info("Inside AuthenticationServiceImpl.authenticate email={}", request.getEmail());

                if (rateLimiterService.isLoginLocked(request.getEmail())) {
                        log.warn("[SECURITY AUDIT] EVENT=LOGIN_BLOCKED email={} reason=too_many_failed_attempts", request.getEmail());
                        throw new ErrorException(HttpStatus.TOO_MANY_REQUESTS, "Too many failed login attempts. Account temporarily locked for 15 minutes.");
                }

                // Authenticate user
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getEmail(),
                                                        request.getPassword()));
                        rateLimiterService.resetFailedLogin(request.getEmail());
                } catch (Exception e) {
                        rateLimiterService.recordFailedLogin(request.getEmail());
                        log.warn("[SECURITY AUDIT] EVENT=LOGIN_FAILED email={}", request.getEmail());
                        throw e;
                }

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
                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put("rank_id", user.getRankId());
                String accessToken = jwtService.generateToken(extraClaims, userDetails);
                String refreshToken = generateRefreshToken(userDetails);

                log.info("[SECURITY AUDIT] EVENT=LOGIN_SUCCESS email={}", user.getEmail());

                return AuthenticationResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .expiresIn(1800000L) // 30 minutes
                                .build();
        }

        @Override
        @Transactional
        public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
                log.info("Inside AuthenticationServiceImpl.refreshToken");

                if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
                        throw new ErrorException(HttpStatus.BAD_REQUEST, "Refresh token is required");
                }

                String hashedToken = TokenHashUtils.hashToken(request.getRefreshToken());

                // Find refresh token by hash
                RefreshTokenEntity refreshToken = refreshTokenRepository.findByTokenHash(hashedToken)
                                .orElseThrow(() -> new ErrorException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

                UserEntity user = refreshToken.getUser();

                // Token Reuse Detection: If token is already revoked, it indicates token theft/replay attack!
                if (refreshToken.isRevoked()) {
                        log.warn("SECURITY ALERT: Refresh token reuse detected for user email={}. Revoking all user sessions!", user.getEmail());
                        refreshTokenRepository.revokeAllUserTokens(user.getId(), LocalDateTime.now());
                        throw new ErrorException(HttpStatus.UNAUTHORIZED, "Security Alert: Refresh token has already been revoked. All active sessions terminated.");
                }

                // Check if token is expired
                if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                        throw new ErrorException(HttpStatus.UNAUTHORIZED, "Refresh token is expired");
                }

                // Generate new tokens
                UserDetails userDetails = createUserDetails(user);
                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put("rank_id", user.getRankId());
                String newAccessToken = jwtService.generateToken(extraClaims, userDetails);
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
                                .expiresIn(1800000L) // 30 minutes
                                .build();
        }

        @Override
        @Transactional
        public void logout(String refreshToken) {
                log.info("Inside AuthenticationServiceImpl.logout");

                if (refreshToken == null || refreshToken.trim().isEmpty()) {
                        return;
                }

                String hashedToken = TokenHashUtils.hashToken(refreshToken);
                RefreshTokenEntity token = refreshTokenRepository.findByTokenHash(hashedToken)
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

                // Revoke all active sessions for this user
                refreshTokenRepository.revokeAllUserTokens(user.getId(), LocalDateTime.now());

                log.info("[SECURITY AUDIT] EVENT=PASSWORD_CHANGED email={}. All active sessions revoked.", email);
        }

        @Override
        @Transactional
        public void resetPassword(ResetPasswordRequest request) {
                log.info("Inside AuthenticationServiceImpl.resetPassword");

                if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                        throw new ErrorException(HttpStatus.BAD_REQUEST, "Reset token is required");
                }

                if (!tokenService.isValidToken(request.getToken())) {
                        throw new ErrorException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token");
                }

                UserEntity user = tokenService.getUserByToken(request.getToken())
                                .orElseThrow(() -> new ErrorException(HttpStatus.BAD_REQUEST, "User not found for token"));

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                // Delete used token
                tokenService.deleteToken(request.getToken());

                // Revoke all active sessions for this user
                refreshTokenRepository.revokeAllUserTokens(user.getId(), LocalDateTime.now());

                log.info("[SECURITY AUDIT] EVENT=PASSWORD_RESET_SUCCESS email={}. All active sessions revoked.", user.getEmail());
        }

        @Override
        @Transactional
        public void forgotPassword(String email) {
                if (email == null || email.trim().isEmpty()) {
                        return;
                }
                String cleanEmail = email.replace("\"", "").trim();
                Optional<UserEntity> user = userRepository.findByEmail(cleanEmail);
                if (user.isPresent()) {
                        PasswordResetToken token = tokenService.createToken(user.get());
                        String resetLink = frontendUrl + "/resetPassword?token=" + token.getToken();
                        try {
                                emailService.sendEmail(cleanEmail, "Password Reset",
                                                "Click here to reset your password: " + resetLink + "\n "
                                                                + "Reset link is valid for 15 minutes");
                                log.info("[SECURITY AUDIT] EVENT=FORGOT_PASSWORD_REQUEST email={}", cleanEmail);
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

                try {
                        if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
                                authorities = user.getUserRoles().stream()
                                                .filter(ur -> ur != null && ur.isActive())
                                                .filter(ur -> ur.getRole() != null && ur.getRole().isActive())
                                                .map(ur -> {
                                                        String roleName = ur.getRole().getRoleName();
                                                        if (roleName != null && !roleName.trim().isEmpty()) {
                                                                return new SimpleGrantedAuthority("ROLE_" + roleName.trim());
                                                        }
                                                        return null;
                                                })
                                                .filter(authority -> authority != null)
                                                .collect(Collectors.toList());
                        }

                        if (authorities.isEmpty()) {
                                authorities.add(new SimpleGrantedAuthority(RoleType.USER.getAuthority()));
                        }
                } catch (Exception e) {
                        log.error("Error building authorities for user {}: {}", user.getEmail(), e.getMessage());
                        authorities.add(new SimpleGrantedAuthority(RoleType.USER.getAuthority()));
                }

                log.info("Inside AuthenticationServiceImpl.createAuthorities email={}, authorities={}", user.getEmail(), authorities);

                return authorities;
        }

        private String generateRefreshToken(UserDetails userDetails) {
                UserEntity user = userRepository.findActiveUserByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

                UUID jti = UUID.randomUUID();
                String refreshToken = jwtService.generateRefreshToken(userDetails);
                String hashedToken = TokenHashUtils.hashToken(refreshToken);

                RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                                .user(user)
                                .jti(jti)
                                .tokenHash(hashedToken)
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
                log.info("Inside AuthenticationServiceImpl.googleLogin");

                if (request.getIdToken() == null || request.getIdToken().trim().isEmpty()) {
                        throw new ErrorException(HttpStatus.BAD_REQUEST, "Google ID token is required");
                }

                FirebaseService.FirebaseUserInfo verifiedInfo;
                try {
                        verifiedInfo = firebaseService.verifyIdToken(request.getIdToken());
                } catch (Exception e) {
                        log.error("Failed to verify Google ID Token: {}", e.getMessage());
                        throw new ErrorException(HttpStatus.UNAUTHORIZED, "Invalid or unverified Google ID token: " + e.getMessage());
                }

                String verifiedEmail = verifiedInfo.getEmail();
                if (verifiedEmail == null || verifiedEmail.trim().isEmpty()) {
                        throw new ErrorException(HttpStatus.UNAUTHORIZED, "Google ID token does not contain a valid email");
                }

                log.info("Google ID token verified for email={}", verifiedEmail);

                // Resolve or create user
                UserEntity user = userRepository.findByEmail(verifiedEmail)
                        .map(existing -> updateExistingGoogleUser(existing, verifiedInfo))
                        .orElseGet(() -> createNewGoogleUser(verifiedEmail, verifiedInfo));

                // Generate JWT tokens
                UserDetails userDetails = createUserDetails(user);
                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put("rank_id", user.getRankId());
                String accessToken = jwtService.generateToken(extraClaims, userDetails);
                String refreshToken = generateRefreshToken(userDetails);

                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .tokenType("Bearer")
                        .expiresIn(1800000L) // 30 minutes
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build();
        }

        private UserEntity updateExistingGoogleUser(UserEntity user, FirebaseService.FirebaseUserInfo verifiedInfo) {
                log.info("Existing Google user found: {}", user.getEmail());
                if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                        String name = verifiedInfo.getName();
                        user.setUsername(name != null
                                ? name.replaceAll("\\s+", "").toLowerCase()
                                : user.getEmail().split("@")[0]);
                }
                if (verifiedInfo.getPicture() != null) {
                        user.setAvatarUrl(verifiedInfo.getPicture());
                }
                user.setEmailVerified(true);
                user.setLastLoginAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                return user;
        }

        private UserEntity createNewGoogleUser(String verifiedEmail, FirebaseService.FirebaseUserInfo verifiedInfo) {
                log.info("Creating new Google user: {}", verifiedEmail);
                String name = verifiedInfo.getName();
                String username = name != null ? name.replaceAll("\\s+", "").toLowerCase() : verifiedEmail.split("@")[0];

                if (userRepository.existsByUsername(username)) {
                        username = username + "_" + UUID.randomUUID().toString().substring(0, 5);
                }

                UserEntity user = UserEntity.builder()
                        .email(verifiedEmail)
                        .username(username)
                        .password("")
                        .avatarUrl(verifiedInfo.getPicture())
                        .isActive(true)
                        .emailVerified(true)
                        .phoneVerified(false)
                        .lastLoginAt(LocalDateTime.now())
                        .build();

                if (user.getUserRoles() == null) {
                        user.setUserRoles(new LinkedHashSet<>());
                }
                if (user.getRefreshTokens() == null) {
                        user.setRefreshTokens(new LinkedHashSet<>());
                }

                UserRank userRank = userRankRepository.findByCode(RankThreshold.BRONZE.name()).orElse(null);
                if (userRank != null) {
                        user.setRankId(userRank.getId());
                }

                user = userRepository.save(user);

                RoleEntity userRole = roleRepository.findByRoleName(RoleType.USER.getRoleName())
                        .orElseThrow(() -> new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Default USER role not found"));

                UserRoleEntity userRoleEntity = UserRoleEntity.builder()
                        .user(user)
                        .role(userRole)
                        .isActive(true)
                        .build();

                user.getUserRoles().add(userRoleEntity);
                return userRepository.save(user);
        }
}

