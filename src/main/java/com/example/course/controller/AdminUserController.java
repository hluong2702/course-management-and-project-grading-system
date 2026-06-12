package com.example.course.controller;

import com.example.course.dto.request.UserUpdateRequest;
import com.example.course.dto.response.ApiResponse;
import com.example.course.dto.response.PageResponse;
import com.example.course.dto.response.UserResponse;
import com.example.course.enums.Role;
import com.example.course.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", userService.getAllUsers(page, size, role, keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User fetched", userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated", userService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.noContent("User deleted"));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status toggled", null));
    }
}
