package com.example.course.service;

import com.example.course.dto.request.CourseRequest;
import com.example.course.dto.response.CourseResponse;
import com.example.course.dto.response.PageResponse;
import com.example.course.entity.Course;
import com.example.course.entity.User;
import com.example.course.enums.Role;
import com.example.course.exception.AppException;
import com.example.course.repository.CourseRepository;
import com.example.course.repository.UserRepository;
import com.example.course.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock CourseRepository courseRepository;
    @Mock UserRepository userRepository;

    @InjectMocks CourseServiceImpl courseService;

    private User mockLecturer;
    private Course mockCourse;

    @BeforeEach
    void setUp() {
        mockLecturer = User.builder()
                .id(10L).username("lecturer1").email("lec@test.com")
                .fullName("Dr. A").role(Role.LECTURER).active(true).build();

        mockCourse = Course.builder()
                .id(1L).name("Java Advanced").code("JAVA301")
                .description("Deep dive into Java").lecturer(mockLecturer)
                .active(true).build();
    }

    @Test
    @DisplayName("Service-11: createCourse with valid data saves and returns response")
    void createCourse_validData_returnsResponse() {
        CourseRequest req = new CourseRequest();
        req.setName("Java Advanced");
        req.setCode("JAVA301");
        req.setDescription("Deep dive into Java");
        req.setLecturerId(10L);

        when(courseRepository.existsByCode("JAVA301")).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(mockLecturer));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CourseResponse resp = courseService.createCourse(req);

        assertThat(resp.getName()).isEqualTo("Java Advanced");
        assertThat(resp.getCode()).isEqualTo("JAVA301");
        assertThat(resp.getLecturer().getUsername()).isEqualTo("lecturer1");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Service-12: createCourse with duplicate code throws 409 Conflict")
    void createCourse_duplicateCode_throwsConflict() {
        CourseRequest req = new CourseRequest();
        req.setName("Another Course");
        req.setCode("JAVA301");

        when(courseRepository.existsByCode("JAVA301")).thenReturn(true);

        assertThatThrownBy(() -> courseService.createCourse(req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Service-13: getAllCourses without keyword returns paged active courses")
    void getAllCourses_noKeyword_returnsPagedCourses() {
        Page<Course> page = new PageImpl<>(List.of(mockCourse),
                PageRequest.of(0, 10), 1);
        when(courseRepository.findByActiveTrue(any(Pageable.class))).thenReturn(page);

        PageResponse<CourseResponse> result = courseService.getAllCourses(0, 10, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("JAVA301");
    }

    @Test
    @DisplayName("Service-14: getAllCourses with keyword calls search query")
    void getAllCourses_withKeyword_callsSearchQuery() {
        Page<Course> page = new PageImpl<>(List.of(mockCourse),
                PageRequest.of(0, 10), 1);
        when(courseRepository.searchByKeyword(eq("Java"), any(Pageable.class))).thenReturn(page);

        PageResponse<CourseResponse> result = courseService.getAllCourses(0, 10, "Java");

        assertThat(result.getContent()).hasSize(1);
        verify(courseRepository).searchByKeyword(eq("Java"), any(Pageable.class));
        verify(courseRepository, never()).findByActiveTrue(any());
    }

    @Test
    @DisplayName("Service-15: getCourseById with nonexistent id throws 404")
    void getCourseById_notFound_throws404() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Service-16: deleteCourse soft-deletes by setting active=false")
    void deleteCourse_existingId_setsActiveFalse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(mockCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        courseService.deleteCourse(1L);

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }
}
