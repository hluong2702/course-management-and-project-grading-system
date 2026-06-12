package com.example.course.service;

import com.example.course.dto.request.*;
import com.example.course.dto.response.AuthResponse;
import com.example.course.dto.response.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String token);
    UserResponse register(RegisterRequest request);
    void changePassword(String username, ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
}
