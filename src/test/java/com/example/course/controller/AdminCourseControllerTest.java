package com.example.course.controller;

import com.example.course.config.TestSecurityConfig;
import com.example.course.dto.request.CourseRequest;
import com.example.course.dto.response.*;
import com.example.course.exception.AppException;
import com.example.course.exception.GlobalExceptionHandler;
import com.example.course.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCourseController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class AdminCourseControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CourseService courseService;

    private CourseResponse buildCourse(Long id, String name, String code) {
        return CourseResponse.builder()
                .id(id).name(name).code(code)
                .description("Description of " + name)
                .active(true).createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("Controller-22: POST /api/v1/admin/courses creates course and returns 201")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCourse_validData_returns201() throws Exception {
        CourseRequest req = new CourseRequest();
        req.setName("Spring Boot");
        req.setCode("SB401");
        req.setDescription("Spring Boot deep dive");

        when(courseService.createCourse(any(CourseRequest.class)))
                .thenReturn(buildCourse(1L, "Spring Boot", "SB401"));

        mockMvc.perform(post("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.code").value("SB401"));
    }

    @Test
    @DisplayName("Controller-23: POST /api/v1/admin/courses with blank name returns 400")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCourse_blankName_returns400() throws Exception {
        CourseRequest req = new CourseRequest();
        req.setName("");        // @NotBlank fails
        req.setCode("SB401");

        mockMvc.perform(post("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Controller-24: POST /api/v1/admin/courses with duplicate code returns 409")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCourse_duplicateCode_returns409() throws Exception {
        CourseRequest req = new CourseRequest();
        req.setName("Duplicate");
        req.setCode("DUP001");

        when(courseService.createCourse(any()))
                .thenThrow(new AppException("Course code already exists", HttpStatus.CONFLICT));

        mockMvc.perform(post("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Course code already exists"));
    }

    @Test
    @DisplayName("Controller-25: GET /api/v1/admin/courses returns paged course list")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getCourses_returnsPaged() throws Exception {
        PageResponse<CourseResponse> page = PageResponse.<CourseResponse>builder()
                .content(List.of(buildCourse(1L, "Java", "JAVA101")))
                .page(0).size(10).totalElements(1L).totalPages(1).last(true).build();

        when(courseService.getAllCourses(0, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/courses")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].code").value("JAVA101"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("Controller-26: DELETE /api/v1/admin/courses/{id} returns 200 (soft delete)")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteCourse_returns200() throws Exception {
        doNothing().when(courseService).deleteCourse(1L);

        mockMvc.perform(delete("/api/v1/admin/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course deactivated"));
    }

    @Test
    @DisplayName("Controller-27: GET /api/v1/admin/courses/{id} for unknown id returns 404")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getCourse_notFound_returns404() throws Exception {
        when(courseService.getCourseById(999L))
                .thenThrow(new AppException("Course not found", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/admin/courses/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Course not found"));
    }
}
