package com.spring.fit.backend.security.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                log.debug("Loading user details for email: {}", email);
                
                try {
                        UserEntity user = userRepository.findActiveUserByEmail(email)
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                        "User not found with email: " + email));

                        log.debug("Found user: id={}, email={}, isActive={}", user.getId(), user.getEmail(), user.isActive());

                        // Create authorities safely
                        List<SimpleGrantedAuthority> authorities = createAuthorities(user);
                        log.debug("Created authorities: {}", authorities);

                        return org.springframework.security.core.userdetails.User.builder()
                                        .username(user.getEmail())
                                        .password(user.getPassword())
                                        .authorities(authorities)
                                        .accountExpired(!user.isActive())
                                        .accountLocked(false)
                                        .credentialsExpired(false)
                                        .disabled(!user.isActive())
                                        .build();
                } catch (UsernameNotFoundException e) {
                        throw e;
                } catch (Exception e) {
                        log.error("Error loading user details for email {}: {}", email, e.getMessage(), e);
                        throw new UsernameNotFoundException("Error loading user details: " + e.getMessage());
                }
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
                                                .toList();
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
}
