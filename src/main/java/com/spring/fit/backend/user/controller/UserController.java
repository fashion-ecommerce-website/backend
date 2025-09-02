package com.spring.fit.backend.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.fit.backend.user.domain.dto.UpdateUserRequest;
import com.spring.fit.backend.user.domain.dto.UserResponse;
import com.spring.fit.backend.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping()
    public ResponseEntity<UserResponse> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse currentUser = userService.getCurrentUser(email);
        return ResponseEntity.ok(currentUser);
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse updatedUser = userService.updateUser(email, request);
        return ResponseEntity.ok(updatedUser);
    }
}
