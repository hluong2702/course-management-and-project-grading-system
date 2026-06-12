package com.example.course.controller;

import com.example.course.config.TestSecurityConfig;
import com.example.course.dto.request.SubmissionRequest;
import com.example.course.dto.response.*;
import com.example.course.enums.Role;
import com.example.course.enums.SubmissionStatus;
import com.example.course.exception.AppException;
import com.example.course.exception.GlobalExceptionHandler;
import com.example.course.service.EnrollmentService;
import com.example.course.service.SubmissionService;
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

@WebMvcTest(StudentController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class StudentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean EnrollmentService enrollmentService;
    @MockBean SubmissionService submissionService;

    private UserResponse mockStudentResp() {
        return UserResponse.builder().id(1L).username("student1")
                .email("s@test.com").role(Role.STUDENT).build();
    }

    @Test
    @DisplayName("Controller-9: POST /api/v1/student/enrollments/course/{id} returns 201 on success")
    @WithMockUser(username = "student1", roles = "STUDENT")
    void enroll_success_returns201() throws Exception {
        CourseResponse courseResp = CourseResponse.builder().id(2L).name("Java").code("JAVA101").build();
        EnrollmentResponse enrollResp = EnrollmentResponse.builder()
                .id(1L).student(mockStudentResp()).course(courseResp)
                .enrolledAt(LocalDateTime.now()).build();

        when(enrollmentService.enroll(eq("student1"), eq(2L))).thenReturn(enrollResp);

        mockMvc.perform(post("/api/v1/student/enrollments/course/2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.course.code").value("JAVA101"));
    }

    @Test
    @DisplayName("Controller-10: POST /api/v1/student/enrollments/course/{id} returns 409 if already enrolled")
    @WithMockUser(username = "student1", roles = "STUDENT")
    void enroll_duplicate_returns409() throws Exception {
        when(enrollmentService.enroll(eq("student1"), eq(2L)))
                .thenThrow(new AppException("Already enrolled in this course", HttpStatus.CONFLICT));

        mockMvc.perform(post("/api/v1/student/enrollments/course/2"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Controller-11: POST /api/v1/student/submissions returns 201 with github link")
    @WithMockUser(username = "student1", roles = "STUDENT")
    void submitGithub_returns201() throws Exception {
        SubmissionRequest req = new SubmissionRequest();
        req.setAssignmentId(1L);
        req.setGithubUrl("https://github.com/student1/project");

        SubmissionResponse resp = SubmissionResponse.builder()
                .id(1L).student(mockStudentResp())
                .assignmentId(1L).assignmentTitle("Assignment 1")
                .githubUrl("https://github.com/student1/project")
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now()).build();

        when(submissionService.submit(eq("student1"), any(SubmissionRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/student/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.githubUrl").value("https://github.com/student1/project"));
    }

    @Test
    @DisplayName("Controller-12: GET /api/v1/student/submissions returns page of submissions")
    @WithMockUser(username = "student1", roles = "STUDENT")
    void getMySubmissions_returnsPaged() throws Exception {
        SubmissionResponse resp = SubmissionResponse.builder()
                .id(1L).student(mockStudentResp())
                .assignmentId(1L).status(SubmissionStatus.GRADED)
                .score(88).submittedAt(LocalDateTime.now()).build();

        PageResponse<SubmissionResponse> page = PageResponse.<SubmissionResponse>builder()
                .content(List.of(resp)).page(0).size(10)
                .totalElements(1L).totalPages(1).last(true).build();

        when(submissionService.getMySubmissions(eq("student1"), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/v1/student/submissions")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].score").value(88))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}
