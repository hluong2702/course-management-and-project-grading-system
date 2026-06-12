package com.example.course.service.impl;

import com.example.course.dto.request.AssignmentRequest;
import com.example.course.dto.response.AssignmentResponse;
import com.example.course.dto.response.PageResponse;
import com.example.course.entity.Assignment;
import com.example.course.entity.Course;
import com.example.course.exception.AppException;
import com.example.course.repository.AssignmentRepository;
import com.example.course.repository.CourseRepository;
import com.example.course.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException("Không tìm thấy khóa học", HttpStatus.NOT_FOUND));
        Assignment assignment = Assignment.builder()
                .title(request.getTitle()).description(request.getDescription())
                .deadline(request.getDeadline()).course(course).build();
        assignmentRepository.save(assignment);
        return mapToResponse(assignment);
    }

    @Override
    public PageResponse<AssignmentResponse> getAssignmentsByCourse(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Assignment> pg = assignmentRepository.findByCourseId(courseId, pageable);
        List<AssignmentResponse> content = pg.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        return PageResponse.<AssignmentResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(pg.getTotalElements()).totalPages(pg.getTotalPages()).last(pg.isLast()).build();
    }

    @Override
    public AssignmentResponse getAssignmentById(Long id) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy bài tập", HttpStatus.NOT_FOUND));
        return mapToResponse(a);
    }

    private AssignmentResponse mapToResponse(Assignment a) {
        return AssignmentResponse.builder()
                .id(a.getId()).title(a.getTitle()).description(a.getDescription())
                .deadline(a.getDeadline()).courseId(a.getCourse().getId())
                .courseName(a.getCourse().getName()).createdAt(a.getCreatedAt()).build();
    }
}
