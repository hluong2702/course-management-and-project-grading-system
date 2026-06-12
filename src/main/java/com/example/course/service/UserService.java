package com.example.course.service;

import com.example.course.dto.request.UserUpdateRequest;
import com.example.course.dto.response.PageResponse;
import com.example.course.dto.response.UserResponse;
import com.example.course.enums.Role;

public interface UserService {
    PageResponse<UserResponse> getAllUsers(int page, int size, Role role, String keyword);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    void toggleUserStatus(Long id);
}
