package com.spring.fit.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.fit.backend.security.jwt.JwtService;

import com.spring.fit.backend.user.domain.dto.request.UpdateUserRequest;
import com.spring.fit.backend.user.domain.dto.response.UserResponse;
import com.spring.fit.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)

class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    // ========================= GET CURRENT USER =========================
    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getCurrentUser_shouldReturnUser() throws Exception {
        UserResponse mockResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .username("tester")
                .build();

        when(userService.getCurrentUser("test@example.com")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("tester"));
    }

    // ========================= UPDATE USER SUCCESS =========================
    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updateUser_validRequest_shouldReturnUpdatedUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newname");
        request.setPhone("0912345678");
        request.setDob(LocalDate.of(2000, 1, 1));

        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .username("newname")
                .phone("0912345678")
                .dob(LocalDate.of(2000, 1, 1))
                .build();

        when(userService.updateUser(any(String.class), any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newname"))
                .andExpect(jsonPath("$.phone").value("0912345678"))
                .andExpect(jsonPath("$.dob").value("2000-01-01"));
    }

    // ========================= INVALID DOB =========================
    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updateUser_futureDob_shouldReturnBadRequest() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("tester");
        request.setPhone("0912345678");
        request.setDob(LocalDate.now().plusDays(1)); // future date

        mockMvc.perform(put("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("dob"))
                .andExpect(jsonPath("$.errors[0].message").value("The date can not be a feature."));
    }

    // ========================= INVALID PHONE =========================
    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updateUser_invalidPhone_shouldReturnBadRequest() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("tester");
        request.setPhone("123"); // invalid phone
        request.setDob(LocalDate.of(2000, 1, 1));

        mockMvc.perform(put("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("phone"))
                .andExpect(jsonPath("$.errors[0].message").value("Invalid phone number format."));
    }
}
