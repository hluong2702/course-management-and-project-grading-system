package com.example.course.controller;

import com.example.course.config.TestSecurityConfig;
import com.example.course.dto.request.LoginRequest;
import com.example.course.dto.request.RefreshTokenRequest;
import com.example.course.dto.request.RegisterRequest;
import com.example.course.dto.response.AuthResponse;
import com.example.course.dto.response.UserResponse;
import com.example.course.enums.Role;
import com.example.course.exception.AppException;
import com.example.course.exception.GlobalExceptionHandler;
import com.example.course.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;

    @Test
    @DisplayName("Controller-1: POST /api/auth/login with valid credentials returns 200 and tokens")
    void login_validCredentials_returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("Password@123");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("access-token").refreshToken("refresh-token")
                .tokenType("Bearer").expiresIn(900000L)
                .user(UserResponse.builder().id(1L).username("student1").role(Role.STUDENT).build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("Controller-2: POST /api/auth/login with bad credentials returns 401")
    void login_badCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("wrongpass");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Controller-3: POST /api/auth/login with missing username returns 400")
    void login_missingUsername_returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        // username blank → @NotBlank fails
        request.setUsername("");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Controller-4: POST /api/auth/register with valid data returns 201")
    void register_validData_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newstudent");
        request.setPassword("Password@123");
        request.setEmail("new@test.com");
        request.setFullName("New Student");

        UserResponse userResponse = UserResponse.builder()
                .id(1L).username("newstudent").email("new@test.com")
                .fullName("New Student").role(Role.STUDENT).active(true).build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.username").value("newstudent"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    @Test
    @DisplayName("Controller-5: POST /api/auth/register with duplicate username returns 409")
    void register_duplicateUsername_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setPassword("Password@123");
        request.setEmail("new@test.com");
        request.setFullName("Someone");

        when(authService.register(any()))
                .thenThrow(new AppException("Username already exists", HttpStatus.CONFLICT));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    @DisplayName("Controller-6: POST /api/auth/register with invalid email returns 400")
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("Password@123");
        request.setEmail("not-an-email");   // invalid
        request.setFullName("New User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Controller-7: POST /api/auth/refresh with invalid token returns 401")
    void refresh_invalidToken_returns401() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-refresh-token");

        when(authService.refreshToken(any()))
                .thenThrow(new AppException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Controller-8: POST /api/auth/logout with valid token returns 200")
    void logout_validToken_returns200() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer valid-token-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }
}
