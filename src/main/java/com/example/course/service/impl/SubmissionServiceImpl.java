package com.example.course.service.impl;

import com.example.course.dto.request.GradeRequest;
import com.example.course.dto.request.SubmissionRequest;
import com.example.course.dto.response.PageResponse;
import com.example.course.dto.response.SubmissionResponse;
import com.example.course.dto.response.UserResponse;
import com.example.course.entity.Assignment;
import com.example.course.entity.Submission;
import com.example.course.entity.User;
import com.example.course.enums.SubmissionStatus;
import com.example.course.exception.AppException;
import com.example.course.repository.AssignmentRepository;
import com.example.course.repository.SubmissionRepository;
import com.example.course.repository.UserRepository;
import com.example.course.service.CloudinaryService;
import com.example.course.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final CloudinaryService cloudinaryService;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Override
    @Transactional
    public SubmissionResponse submit(String username, SubmissionRequest request) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND));
        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new AppException("Bài tập không tồn tại", HttpStatus.NOT_FOUND));

        if (submissionRepository.existsByStudentIdAndAssignmentId(student.getId(), request.getAssignmentId())) {
            throw new AppException("Đã nộp bài cho bài tập này", HttpStatus.CONFLICT);
        }

        SubmissionStatus status = SubmissionStatus.SUBMITTED;
        if (assignment.getDeadline() != null && LocalDateTime.now().isAfter(assignment.getDeadline())) {
            status = SubmissionStatus.LATE;
        }

        Submission submission = Submission.builder()
                .student(student)
                .assignment(assignment)
                .githubUrl(request.getGithubUrl())
                .status(status)
                .build();

        submissionRepository.save(submission);
        return mapToResponse(submission);
    }

    @Override
    @Transactional
    public SubmissionResponse uploadReport(String username, Long assignmentId, MultipartFile file) {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new AppException("Chỉ cho phép tải lên tệp PDF và Word", HttpStatus.BAD_REQUEST);
        }

        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND));

        Submission submission = submissionRepository.findByStudentIdAndAssignmentId(student.getId(), assignmentId)
                .orElseThrow(() -> new AppException("Không tìm thấy bài nộp. Vui lòng nộp bài trước.", HttpStatus.NOT_FOUND));

        String fileUrl = cloudinaryService.uploadFile(file, "submissions");
        submission.setReportUrl(fileUrl);
        submissionRepository.save(submission);
        return mapToResponse(submission);
    }

    @Override
    @Transactional
    public SubmissionResponse gradeSubmission(GradeRequest request) {
        Submission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new AppException("Bài nộp không tồn tại", HttpStatus.NOT_FOUND));

        if (submission.getStatus() == SubmissionStatus.PENDING) {
            throw new AppException("Không thể chấm điểm: sinh viên chưa nộp bài", HttpStatus.BAD_REQUEST);
        }

        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());

        submissionRepository.save(submission);
        return mapToResponse(submission);
    }

    @Override
    public PageResponse<SubmissionResponse> getSubmissionsByAssignment(Long assignmentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<Submission> pg = submissionRepository.findByAssignmentId(assignmentId, pageable);
        List<SubmissionResponse> content = pg.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        return PageResponse.<SubmissionResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(pg.getTotalElements()).totalPages(pg.getTotalPages()).last(pg.isLast()).build();
    }

    @Override
    public PageResponse<SubmissionResponse> getMySubmissions(String username, int page, int size) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<Submission> pg = submissionRepository.findByStudentId(student.getId(), pageable);
        List<SubmissionResponse> content = pg.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        return PageResponse.<SubmissionResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(pg.getTotalElements()).totalPages(pg.getTotalPages()).last(pg.isLast()).build();
    }

    private SubmissionResponse mapToResponse(Submission s) {
        UserResponse studentResp = UserResponse.builder()
                .id(s.getStudent().getId()).username(s.getStudent().getUsername())
                .email(s.getStudent().getEmail()).fullName(s.getStudent().getFullName())
                .role(s.getStudent().getRole()).build();
        return SubmissionResponse.builder()
                .id(s.getId()).student(studentResp)
                .assignmentId(s.getAssignment().getId())
                .assignmentTitle(s.getAssignment().getTitle())
                .githubUrl(s.getGithubUrl()).reportUrl(s.getReportUrl())
                .status(s.getStatus()).score(s.getScore()).feedback(s.getFeedback())
                .submittedAt(s.getSubmittedAt()).gradedAt(s.getGradedAt()).build();
    }
}
