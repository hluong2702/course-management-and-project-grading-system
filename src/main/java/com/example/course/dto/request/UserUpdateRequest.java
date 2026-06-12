package com.example.course.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String fullName;
    @Email
    private String email;
    private Boolean active;
}
