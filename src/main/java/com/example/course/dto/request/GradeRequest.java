package com.example.course.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeRequest {
    @NotNull
    private Long submissionId;
    @NotNull
    @Min(0) @Max(100)
    private Integer score;
    private String feedback;
}
