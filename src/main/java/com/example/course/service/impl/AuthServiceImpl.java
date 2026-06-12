package com.example.course.service.impl;

import com.example.course.dto.request.*;
import com.example.course.dto.response.AuthResponse;
import com.example.course.dto.response.UserResponse;
import com.example.course.entity.RefreshToken;
import com.example.course.entity.User;
import com.example.course.enums.Role;
import com.example.course.exception.AppException;
import com.example.course.repository.RefreshTokenRepository;
import com.example.course.repository.UserRepository;
import com.example.course.service.AuthService;
import com.example.course.service.TokenBlacklistService;
import com.example.course.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng", HttpStatus.NOT_FOUND));

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Thu hồi tất cả refresh token cũ của người dùng
        refreshTokenRepository.revokeAllByUserId(user.getId());

        // Lưu refresh token mới
        RefreshToken rt = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken rt = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException("Refresh token không hợp lệ", HttpStatus.UNAUTHORIZED));

        if (rt.isRevoked() || rt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException("Refresh token đã hết hạn hoặc đã bị thu hồi", HttpStatus.UNAUTHORIZED);
        }

        User user = rt.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        RefreshToken newRt = RefreshToken.builder()
                .token(newRefreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRt);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    public void logout(String token) {
        Date expiration = jwtUtil.extractExpiration(token);
        long ttl = expiration.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            tokenBlacklistService.blacklist(token, ttl);
        }
        log.info("Người dùng đã đăng xuất, token đã được đưa vào danh sách đen");
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("Tên đăng nhập đã tồn tại", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email đã tồn tại", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(Role.STUDENT)
                .active(true)
                .build();

        userRepository.save(user);
        log.info("Sinh viên mới đăng kí: {}", user.getUsername());
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException("Mật khẩu hiện tại không chính xác", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("Email không tồn tại", HttpStatus.NOT_FOUND));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
