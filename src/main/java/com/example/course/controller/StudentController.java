package com.example.course.controller;

import com.example.course.dto.request.SubmissionRequest;
import com.example.course.dto.response.*;
import com.example.course.service.EnrollmentService;
import com.example.course.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentController {

    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;

    //  Đăng ký khóa học
    @PostMapping("/enrollments/course/{courseId}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long courseId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Enrolled successfully",
                        enrollmentService.enroll(userDetails.getUsername(), courseId)));
    }

    // Xem danh sách khóa học đã đăng ký
    @GetMapping("/enrollments")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getMyEnrollments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Enrollments fetched",
                enrollmentService.getMyEnrollments(userDetails.getUsername(), page, size)));
    }

    // Nộp bài tập (Liên kết GitHub)
    @PostMapping("/submissions")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SubmissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Submission created",
                        submissionService.submit(userDetails.getUsername(), request)));
    }

    // Tải lên file báo cáo
    @PostMapping("/submissions/upload")
    public ResponseEntity<ApiResponse<SubmissionResponse>> uploadReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long assignmentId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Report uploaded",
                submissionService.uploadReport(userDetails.getUsername(), assignmentId, file)));
    }

    // Xem danh sách bài nộp, điểm và phản hồi của sinh viên
    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<PageResponse<SubmissionResponse>>> getMySubmissions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Submissions fetched",
                submissionService.getMySubmissions(userDetails.getUsername(), page, size)));
    }
}
