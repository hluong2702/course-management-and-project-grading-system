package com.example.course.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(200).message(message).data(data)
                .timestamp(LocalDateTime.now()).build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .status(201).message(message).data(data)
                .timestamp(LocalDateTime.now()).build();
    }

    public static ApiResponse<Void> noContent(String message) {
        return ApiResponse.<Void>builder()
                .status(204).message(message)
                .timestamp(LocalDateTime.now()).build();
    }
}
