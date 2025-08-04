package com.spring.fit.backend.security.config;

import com.spring.fit.backend.security.entity.Role;
import com.spring.fit.backend.security.entity.User;
import com.spring.fit.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@fit.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            
            userRepository.save(adminUser);
            log.info("Admin user created successfully");
        }

        // Create test user if not exists
        if (!userRepository.existsByUsername("user")) {
            User testUser = User.builder()
                    .username("user")
                    .email("user@fit.com")
                    .password(passwordEncoder.encode("user123"))
                    .firstName("Test")
                    .lastName("User")
                    .role(Role.USER)
                    .enabled(true)
                    .build();
            
            userRepository.save(testUser);
            log.info("Test user created successfully");
        }

        log.info("Data initialization completed");
    }
} 