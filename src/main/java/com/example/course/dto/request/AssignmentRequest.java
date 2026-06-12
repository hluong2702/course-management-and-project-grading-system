package com.example.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentRequest {
    @NotBlank
    private String title;
    private String description;
    private LocalDateTime deadline;
    @NotNull
    private Long courseId;
}
