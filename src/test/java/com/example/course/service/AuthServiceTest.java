package com.example.course.service;

import com.example.course.dto.request.LoginRequest;
import com.example.course.dto.request.RegisterRequest;
import com.example.course.dto.response.AuthResponse;
import com.example.course.dto.response.UserResponse;
import com.example.course.entity.User;
import com.example.course.enums.Role;
import com.example.course.exception.AppException;
import com.example.course.repository.RefreshTokenRepository;
import com.example.course.repository.UserRepository;
import com.example.course.service.impl.AuthServiceImpl;
import com.example.course.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock TokenBlacklistService tokenBlacklistService;

    @InjectMocks AuthServiceImpl authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("student1")
                .password("encoded_password")
                .email("student1@test.com")
                .fullName("Student One")
                .role(Role.STUDENT)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Service-1: Login with valid credentials returns AuthResponse with tokens")
    void login_validCredentials_returnsAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("password123");

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateAccessToken(mockUser)).thenReturn("access-token-123");
        when(jwtUtil.generateRefreshToken(mockUser)).thenReturn("refresh-token-456");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Service-2: Login with invalid credentials throws BadCredentialsException")
    void login_invalidCredentials_throwsBadCredentialsException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("wrong_password");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Service-3: Register with duplicate username throws AppException with 409")
    void register_duplicateUsername_throwsConflictException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("student1");
        request.setPassword("password123");
        request.setEmail("new@test.com");
        request.setFullName("New Student");

        when(userRepository.existsByUsername("student1")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("Service-4: Register with duplicate email throws AppException with 409")
    void register_duplicateEmail_throwsConflictException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("student1@test.com");
        request.setFullName("New Student");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("student1@test.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("Service-5: Register with valid data saves user with STUDENT role and encoded password")
    void register_validData_savesUserWithStudentRole() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newstudent");
        request.setPassword("password123");
        request.setEmail("new@test.com");
        request.setFullName("New Student");

        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserResponse response = authService.register(request);

        // Assert
        assertThat(response.getUsername()).isEqualTo("newstudent");
        assertThat(response.getRole()).isEqualTo(Role.STUDENT);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed_password");
        assertThat(captor.getValue().getRole()).isEqualTo(Role.STUDENT);
    }
}
