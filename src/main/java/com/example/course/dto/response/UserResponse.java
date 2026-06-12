package com.example.course.dto.response;

import com.example.course.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
}
