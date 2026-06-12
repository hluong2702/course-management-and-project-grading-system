package com.example.course.service.impl;

import com.example.course.dto.request.CourseRequest;
import com.example.course.dto.response.CourseResponse;
import com.example.course.dto.response.PageResponse;
import com.example.course.dto.response.UserResponse;
import com.example.course.entity.Course;
import com.example.course.entity.User;
import com.example.course.exception.AppException;
import com.example.course.repository.CourseRepository;
import com.example.course.repository.UserRepository;
import com.example.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCode(request.getCode())) {
            throw new AppException("Mã khóa học đã tồn tại", HttpStatus.CONFLICT);
        }

        User lecturer = null;
        if (request.getLecturerId() != null) {
            lecturer = userRepository.findById(request.getLecturerId())
                    .orElseThrow(() -> new AppException("Giảng viên không tồn tại", HttpStatus.NOT_FOUND));
        }

        Course course = Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .code(request.getCode())
                .lecturer(lecturer)
                .active(true)
                .build();

        courseRepository.save(course);
        return mapToResponse(course);
    }

    @Override
    public PageResponse<CourseResponse> getAllCourses(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> coursePage;

        if (keyword != null && !keyword.isBlank()) {
            coursePage = courseRepository.searchByKeyword(keyword, pageable);
        } else {
            coursePage = courseRepository.findByActiveTrue(pageable);
        }

        List<CourseResponse> content = coursePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<CourseResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(coursePage.getTotalElements())
                .totalPages(coursePage.getTotalPages())
                .last(coursePage.isLast())
                .build();
    }

    @Override
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException("Khóa học không tồn tại", HttpStatus.NOT_FOUND));
        return mapToResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException("Khóa học không tồn tại", HttpStatus.NOT_FOUND));

        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCode(request.getCode());

        if (request.getLecturerId() != null) {
            User lecturer = userRepository.findById(request.getLecturerId())
                    .orElseThrow(() -> new AppException("Giảng viên không tồn tại", HttpStatus.NOT_FOUND));
            course.setLecturer(lecturer);
        }

        courseRepository.save(course);
        return mapToResponse(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException("Khóa học không tồn tại", HttpStatus.NOT_FOUND));
        course.setActive(false);
        courseRepository.save(course);
    }

    private CourseResponse mapToResponse(Course course) {
        UserResponse lecturerResponse = course.getLecturer() == null ? null :
                UserResponse.builder()
                        .id(course.getLecturer().getId())
                        .username(course.getLecturer().getUsername())
                        .email(course.getLecturer().getEmail())
                        .fullName(course.getLecturer().getFullName())
                        .role(course.getLecturer().getRole())
                        .build();

        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .code(course.getCode())
                .lecturer(lecturerResponse)
                .active(course.isActive())
                .createdAt(course.getCreatedAt())
                .build();
    }
}
