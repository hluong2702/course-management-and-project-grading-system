package com.example.course.service;

import com.example.course.dto.response.EnrollmentResponse;
import com.example.course.dto.response.PageResponse;

public interface EnrollmentService {
    EnrollmentResponse enroll(String username, Long courseId);
    PageResponse<EnrollmentResponse> getMyEnrollments(String username, int page, int size);
    PageResponse<EnrollmentResponse> getCourseEnrollments(Long courseId, int page, int size);
}
