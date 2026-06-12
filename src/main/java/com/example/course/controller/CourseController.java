package com.example.course.controller;

import com.example.course.dto.response.ApiResponse;
import com.example.course.dto.response.AssignmentResponse;
import com.example.course.dto.response.CourseResponse;
import com.example.course.dto.response.LectureMaterialResponse;
import com.example.course.dto.response.PageResponse;
import com.example.course.service.AssignmentService;
import com.example.course.service.CourseService;
import com.example.course.service.EnrollmentService;
import com.example.course.service.LectureMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final LectureMaterialService lectureMaterialService;
    private final EnrollmentService enrollmentService;

    // Liệt kê tất cả các khóa học đang hoạt động (có phân trang, có thể tìm kiếm)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success("Courses fetched",
                courseService.getAllCourses(page, size, keyword)));
    }

    // Lấy thông tin chi tiết về một khóa học
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Course fetched",
                courseService.getCourseById(id)));
    }

    // Liệt kê các bài tập trong một khóa học
    @GetMapping("/{courseId}/assignments")
    public ResponseEntity<ApiResponse<PageResponse<AssignmentResponse>>> getAssignments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Assignments fetched",
                assignmentService.getAssignmentsByCourse(courseId, page, size)));
    }

    // Lấy thông tin chi tiết về một bài tập
    @GetMapping("/assignments/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Assignment fetched",
                assignmentService.getAssignmentById(id)));
    }

    // Liệt kê tài liệu giảng dạy cho một khóa học
    @GetMapping("/{courseId}/materials")
    public ResponseEntity<ApiResponse<PageResponse<LectureMaterialResponse>>> getMaterials(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Materials fetched",
                lectureMaterialService.getMaterialsByCourse(courseId, page, size)));
    }

    // Liệt kê sinh viên đăng ký trong một khóa học (ADMIN / LECTURER)
    @GetMapping("/{courseId}/enrollments")
    public ResponseEntity<ApiResponse<?>> getCourseEnrollments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Enrollments fetched",
                enrollmentService.getCourseEnrollments(courseId, page, size)));
    }
}
