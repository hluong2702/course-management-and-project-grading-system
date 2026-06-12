package com.example.course.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class EnrollmentResponse {
    private Long id;
    private UserResponse student;
    private CourseResponse course;
    private LocalDateTime enrolledAt;
}
