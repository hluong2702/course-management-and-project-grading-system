package com.example.course.service;

import com.example.course.dto.request.UserUpdateRequest;
import com.example.course.dto.response.PageResponse;
import com.example.course.dto.response.UserResponse;
import com.example.course.entity.User;
import com.example.course.enums.Role;
import com.example.course.exception.AppException;
import com.example.course.repository.UserRepository;
import com.example.course.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;

    @InjectMocks UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L).username("student1").email("student1@test.com")
                .fullName("Student One").role(Role.STUDENT).active(true).build();
    }

    @Test
    @DisplayName("Service-17: getUserById returns UserResponse for valid id")
    void getUserById_validId_returnsResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserResponse resp = userService.getUserById(1L);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getUsername()).isEqualTo("student1");
        assertThat(resp.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    @DisplayName("Service-18: getUserById with nonexistent id throws 404")
    void getUserById_notFound_throws404() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Service-19: updateUser patches fullName and email correctly")
    void updateUser_validRequest_updatesFields() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setFullName("Updated Name");
        req.setEmail("updated@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByEmail("updated@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse resp = userService.updateUser(1L, req);

        assertThat(resp.getFullName()).isEqualTo("Updated Name");
        assertThat(resp.getEmail()).isEqualTo("updated@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Service-20: toggleUserStatus flips active flag from true to false")
    void toggleUserStatus_activeUser_deactivates() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.toggleUserStatus(1L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    @DisplayName("Service-21: getAllUsers with STUDENT role filters correctly")
    void getAllUsers_withStudentRole_filtersCorrectly() {
        Page<User> page = new PageImpl<>(List.of(mockUser), PageRequest.of(0, 10), 1);
        when(userRepository.findByRole(eq(Role.STUDENT), any(Pageable.class))).thenReturn(page);

        PageResponse<UserResponse> result = userService.getAllUsers(0, 10, Role.STUDENT, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getRole()).isEqualTo(Role.STUDENT);
        verify(userRepository).findByRole(eq(Role.STUDENT), any(Pageable.class));
    }

    @Test
    @DisplayName("Service-22: deleteUser throws 404 for nonexistent id")
    void deleteUser_notFound_throws404() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(userRepository, never()).deleteById(any());
    }
}
