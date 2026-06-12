package com.example.course.controller;

import com.example.course.config.TestSecurityConfig;
import com.example.course.dto.request.UserUpdateRequest;
import com.example.course.dto.response.*;
import com.example.course.enums.Role;
import com.example.course.exception.AppException;
import com.example.course.exception.GlobalExceptionHandler;
import com.example.course.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class AdminUserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserService userService;

    private UserResponse buildUser(Long id, String username, Role role) {
        return UserResponse.builder()
                .id(id).username(username).email(username + "@test.com")
                .fullName("User " + id).role(role).active(true)
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("Controller-16: GET /api/v1/admin/users returns paged user list")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUsers_returns200WithPage() throws Exception {
        PageResponse<UserResponse> page = PageResponse.<UserResponse>builder()
                .content(List.of(buildUser(1L, "student1", Role.STUDENT)))
                .page(0).size(10).totalElements(1L).totalPages(1).last(true).build();

        when(userService.getAllUsers(0, 10, null, null)).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("student1"));
    }

    @Test
    @DisplayName("Controller-17: GET /api/v1/admin/users/{id} returns user")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserById_returns200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(buildUser(1L, "student1", Role.STUDENT));

        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    @Test
    @DisplayName("Controller-18: GET /api/v1/admin/users/{id} for nonexistent user returns 404")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new AppException("User not found", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/admin/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Controller-19: PUT /api/v1/admin/users/{id} updates user")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateUser_returns200() throws Exception {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setFullName("Updated Name");
        req.setEmail("updated@test.com");

        UserResponse updated = UserResponse.builder()
                .id(1L).username("student1").email("updated@test.com")
                .fullName("Updated Name").role(Role.STUDENT).active(true).build();

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.data.email").value("updated@test.com"));
    }

    @Test
    @DisplayName("Controller-20: PATCH /api/v1/admin/users/{id}/toggle-status returns 200")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void toggleStatus_returns200() throws Exception {
        doNothing().when(userService).toggleUserStatus(1L);

        mockMvc.perform(patch("/api/v1/admin/users/1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User status toggled"));
    }

    @Test
    @DisplayName("Controller-21: DELETE /api/v1/admin/users/{id} returns 200 on success")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteUser_returns200() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted"));
    }
}
