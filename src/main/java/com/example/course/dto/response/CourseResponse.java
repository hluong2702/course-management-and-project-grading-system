package com.example.course.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class CourseResponse {
    private Long id;
    private String name;
    private String description;
    private String code;
    private UserResponse lecturer;
    private boolean active;
    private LocalDateTime createdAt;
}
