package com.example.course.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class AssignmentResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Long courseId;
    private String courseName;
    private LocalDateTime createdAt;
}
