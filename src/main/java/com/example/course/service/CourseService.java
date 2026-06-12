package com.example.course.service;

import com.example.course.dto.request.CourseRequest;
import com.example.course.dto.response.CourseResponse;
import com.example.course.dto.response.PageResponse;

public interface CourseService {
    CourseResponse createCourse(CourseRequest request);
    PageResponse<CourseResponse> getAllCourses(int page, int size, String keyword);
    CourseResponse getCourseById(Long id);
    CourseResponse updateCourse(Long id, CourseRequest request);
    void deleteCourse(Long id);
}
