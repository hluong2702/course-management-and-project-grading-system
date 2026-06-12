package com.example.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Tên người dùng là bắt buộc")
    private String username;
    @NotBlank(message = "Mật khẩu là bắt buộc")
    private String password;
}
