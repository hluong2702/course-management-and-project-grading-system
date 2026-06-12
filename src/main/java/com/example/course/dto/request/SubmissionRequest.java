package com.example.course.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionRequest {
    @NotNull
    private Long assignmentId;
    private String githubUrl;
}
