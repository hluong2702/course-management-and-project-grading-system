package com.example.course.controller;

import com.example.course.config.TestSecurityConfig;
import com.example.course.dto.request.GradeRequest;
import com.example.course.dto.response.*;
import com.example.course.enums.Role;
import com.example.course.enums.SubmissionStatus;
import com.example.course.exception.AppException;
import com.example.course.exception.GlobalExceptionHandler;
import com.example.course.service.AssignmentService;
import com.example.course.service.LectureMaterialService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LecturerController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class LecturerControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AssignmentService assignmentService;
    @MockBean SubmissionService submissionService;
    @MockBean LectureMaterialService lectureMaterialService;

    @Test
    @DisplayName("Controller-13: POST /api/v1/lecturer/grades returns 200 with graded submission")
    @WithMockUser(username = "lecturer1", roles = "LECTURER")
    void grade_validRequest_returns200() throws Exception {
        GradeRequest req = new GradeRequest();
        req.setSubmissionId(1L);
        req.setScore(92);
        req.setFeedback("Excellent!");

        UserResponse studentResp = UserResponse.builder()
                .id(2L).username("student1").role(Role.STUDENT).build();

        SubmissionResponse resp = SubmissionResponse.builder()
                .id(1L).student(studentResp)
                .assignmentId(1L).assignmentTitle("Assignment 1")
                .status(SubmissionStatus.GRADED)
                .score(92).feedback("Excellent!")
                .gradedAt(LocalDateTime.now()).build();

        when(submissionService.gradeSubmission(any(GradeRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/lecturer/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.score").value(92))
                .andExpect(jsonPath("$.data.status").value("GRADED"))
                .andExpect(jsonPath("$.data.feedback").value("Excellent!"));
    }

    @Test
    @DisplayName("Controller-14: POST /api/v1/lecturer/grades with score > 100 returns 400")
    @WithMockUser(username = "lecturer1", roles = "LECTURER")
    void grade_scoreOutOfRange_returns400() throws Exception {
        GradeRequest req = new GradeRequest();
        req.setSubmissionId(1L);
        req.setScore(110);   // exceeds @Max(100)
        req.setFeedback("Out of range");

        mockMvc.perform(post("/api/v1/lecturer/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Controller-15: POST /api/v1/lecturer/grades for PENDING submission returns 400")
    @WithMockUser(username = "lecturer1", roles = "LECTURER")
    void grade_pendingSubmission_returns400() throws Exception {
        GradeRequest req = new GradeRequest();
        req.setSubmissionId(99L);
        req.setScore(80);

        when(submissionService.gradeSubmission(any()))
                .thenThrow(new AppException("Cannot grade: student has not submitted yet", HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/api/v1/lecturer/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
