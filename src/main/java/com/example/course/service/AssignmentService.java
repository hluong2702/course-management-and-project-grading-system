package com.example.course.service;

import com.example.course.dto.request.AssignmentRequest;
import com.example.course.dto.response.AssignmentResponse;
import com.example.course.dto.response.PageResponse;

public interface AssignmentService {
    AssignmentResponse createAssignment(AssignmentRequest request);
    PageResponse<AssignmentResponse> getAssignmentsByCourse(Long courseId, int page, int size);
    AssignmentResponse getAssignmentById(Long id);
}
