package com.example.course.controller;

import com.example.course.dto.request.AssignmentRequest;
import com.example.course.dto.request.GradeRequest;
import com.example.course.dto.response.*;
import com.example.course.service.AssignmentService;
import com.example.course.service.LectureMaterialService;
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
@RequestMapping("/api/v1/lecturer")
@RequiredArgsConstructor
public class LecturerController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final LectureMaterialService lectureMaterialService;

    // Bài tập
    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Assignment created", assignmentService.createAssignment(request)));
    }

    //
    @GetMapping("/assignments/course/{courseId}")
    public ResponseEntity<ApiResponse<PageResponse<AssignmentResponse>>> getAssignments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Assignments fetched",
                assignmentService.getAssignmentsByCourse(courseId, page, size)));
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Assignment fetched", assignmentService.getAssignmentById(id)));
    }

    // Chấm điểm
    @PostMapping("/grades")
    public ResponseEntity<ApiResponse<SubmissionResponse>> gradeSubmission(
            @Valid @RequestBody GradeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Submission graded",
                submissionService.gradeSubmission(request)));
    }

    @GetMapping("/submissions/{assignmentId}")
    public ResponseEntity<ApiResponse<PageResponse<SubmissionResponse>>> getSubmissions(
            @PathVariable Long assignmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Submissions fetched",
                submissionService.getSubmissionsByAssignment(assignmentId, page, size)));
    }

    // Tài liệu giảng dạy (FR-09)
    @PostMapping("/materials/course/{courseId}")
    public ResponseEntity<ApiResponse<LectureMaterialResponse>> uploadMaterial(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long courseId,
            @RequestParam String title,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Material uploaded",
                        lectureMaterialService.uploadMaterial(userDetails.getUsername(), courseId, title, file)));
    }

    @GetMapping("/materials/course/{courseId}")
    public ResponseEntity<ApiResponse<PageResponse<LectureMaterialResponse>>> getMaterials(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Materials fetched",
                lectureMaterialService.getMaterialsByCourse(courseId, page, size)));
    }
}
