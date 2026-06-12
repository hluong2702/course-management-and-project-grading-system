package com.example.course.dto.response;

import com.example.course.enums.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class SubmissionResponse {
    private Long id;
    private UserResponse student;
    private Long assignmentId;
    private String assignmentTitle;
    private String githubUrl;
    private String reportUrl;
    private SubmissionStatus status;
    private Integer score;
    private String feedback;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
}
