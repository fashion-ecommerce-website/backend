package com.spring.fit.backend.user.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.user.domain.dto.UpdateUserRequest;
import com.spring.fit.backend.user.domain.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UpdateUserRequest request;

    private UserEntity mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new UserEntity();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("oldUser");
        mockUser.setPhone("0901234567");
        mockUser.setDob(LocalDate.of(1999, 1, 1));
        mockUser.setActive(true);


    }

    @Test
    void getCurrentUser_success() {
        when(userRepository.findActiveUserByEmail("test@example.com"))
                .thenReturn(Optional.of(mockUser));

        UserResponse response = userService.getCurrentUser("test@example.com");

        assertNotNull(response);
        assertEquals("oldUser", response.getUsername());
        assertEquals("0901234567", response.getPhone());
    }

    @Test
    void getCurrentUser_invalidEmail_throwsError() {
        ErrorException ex = assertThrows(ErrorException.class,
                () -> userService.getCurrentUser("   "));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void getCurrentUser_notFound_throwsError() {
        when(userRepository.findActiveUserByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        ErrorException ex = assertThrows(ErrorException.class,
                () -> userService.getCurrentUser("notfound@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void updateUser_success() {
        when(userRepository.findActiveUserByEmail("test@example.com"))
                .thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("newUser")
                .phone("0912345678")
                .dob(LocalDate.of(2000, 1, 1))
                .avatarUrl("http://avatar.com/me.png")
                .build();

        UserResponse response = userService.updateUser("test@example.com", request);

        assertNotNull(response);
        assertEquals("newUser", response.getUsername());
        assertEquals("0912345678", response.getPhone());
        assertEquals("http://avatar.com/me.png", response.getAvatarUrl());
    }


    @Test
    void updateUser_conflictUsername_throwsError() {
        when(userRepository.findActiveUserByEmail("test@example.com"))
                .thenReturn(Optional.of(mockUser));
        when(userRepository.existsByUsername("newUser")).thenReturn(true);

        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("newUser")
                .build();

        ErrorException ex = assertThrows(ErrorException.class,
                () -> userService.updateUser("test@example.com", request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void updateUser_invalidRequest_throwsError() {
        ErrorException ex = assertThrows(ErrorException.class,
                () -> userService.updateUser("   ", null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
