package com.example.course.service.impl;

import com.example.course.dto.response.*;
import com.example.course.entity.*;
import com.example.course.exception.AppException;
import com.example.course.repository.*;
import com.example.course.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public EnrollmentResponse enroll(String username, Long courseId) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("Khóa học không tồn tại", HttpStatus.NOT_FOUND));

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new AppException("Đã đăng ký khóa học này", HttpStatus.CONFLICT);
        }

        Enrollment enrollment = Enrollment.builder().student(student).course(course).build();
        enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    @Override
    public PageResponse<EnrollmentResponse> getMyEnrollments(String username, int page, int size) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size, Sort.by("enrolledAt").descending());
        Page<Enrollment> pg = enrollmentRepository.findByStudentId(student.getId(), pageable);
        List<EnrollmentResponse> content = pg.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        return PageResponse.<EnrollmentResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(pg.getTotalElements()).totalPages(pg.getTotalPages()).last(pg.isLast()).build();
    }

    @Override
    public PageResponse<EnrollmentResponse> getCourseEnrollments(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("enrolledAt").descending());
        Page<Enrollment> pg = enrollmentRepository.findByCourseId(courseId, pageable);
        List<EnrollmentResponse> content = pg.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        return PageResponse.<EnrollmentResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(pg.getTotalElements()).totalPages(pg.getTotalPages()).last(pg.isLast()).build();
    }

    private EnrollmentResponse mapToResponse(Enrollment e) {
        UserResponse studentResp = UserResponse.builder()
                .id(e.getStudent().getId()).username(e.getStudent().getUsername())
                .email(e.getStudent().getEmail()).fullName(e.getStudent().getFullName())
                .role(e.getStudent().getRole()).build();
        CourseResponse courseResp = CourseResponse.builder()
                .id(e.getCourse().getId()).name(e.getCourse().getName())
                .code(e.getCourse().getCode()).build();
        return EnrollmentResponse.builder()
                .id(e.getId()).student(studentResp).course(courseResp).enrolledAt(e.getEnrolledAt()).build();
    }
}
