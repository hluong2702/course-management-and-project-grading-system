package com.example.course.controller;

import com.example.course.dto.request.CourseRequest;
import com.example.course.dto.response.ApiResponse;
import com.example.course.dto.response.CourseResponse;
import com.example.course.dto.response.PageResponse;
import com.example.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Course created", courseService.createCourse(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success("Courses fetched", courseService.getAllCourses(page, size, keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Course fetched", courseService.getCourseById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Course updated", courseService.updateCourse(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.noContent("Course deactivated"));
    }
}
