package com.example.course.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class LectureMaterialResponse {
    private Long id;
    private String title;
    private String fileUrl;
    private String fileName;
    private Long courseId;
    private String courseName;
    private LocalDateTime uploadedAt;
}
