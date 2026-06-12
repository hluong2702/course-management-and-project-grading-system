package com.example.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank(message = "Tên khóa học là bắt buộc")
    private String name;
    private String description;
    @NotBlank(message = "Mã khóa học là bắt buộc")
    private String code;
    private Long lecturerId;
}
