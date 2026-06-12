package com.example.course.service;

import com.example.course.dto.request.GradeRequest;
import com.example.course.dto.request.SubmissionRequest;
import com.example.course.dto.response.SubmissionResponse;
import com.example.course.entity.*;
import com.example.course.enums.Role;
import com.example.course.enums.SubmissionStatus;
import com.example.course.exception.AppException;
import com.example.course.repository.*;
import com.example.course.service.impl.SubmissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock SubmissionRepository submissionRepository;
    @Mock UserRepository userRepository;
    @Mock AssignmentRepository assignmentRepository;
    @Mock CloudinaryService cloudinaryService;

    @InjectMocks SubmissionServiceImpl submissionService;

    private User mockStudent;
    private Assignment mockAssignment;
    private Course mockCourse;

    @BeforeEach
    void setUp() {
        mockStudent = User.builder()
                .id(1L).username("student1").email("s@test.com")
                .fullName("Student One").role(Role.STUDENT).active(true).build();

        mockCourse = Course.builder().id(1L).name("Java Course").code("JAVA101").build();

        mockAssignment = Assignment.builder()
                .id(1L).title("Assignment 1").course(mockCourse)
                .deadline(LocalDateTime.now().plusDays(7)).build();
    }

    @Test
    @DisplayName("Service-6: Submit with github URL sets status SUBMITTED when before deadline")
    void submit_beforeDeadline_statusIsSubmitted() {
        // Arrange
        SubmissionRequest request = new SubmissionRequest();
        request.setAssignmentId(1L);
        request.setGithubUrl("https://github.com/student/project");

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(mockStudent));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(mockAssignment));
        when(submissionRepository.existsByStudentIdAndAssignmentId(1L, 1L)).thenReturn(false);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> {
            Submission s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        // Act
        SubmissionResponse response = submissionService.submit("student1", request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(response.getGithubUrl()).isEqualTo("https://github.com/student/project");
    }

    @Test
    @DisplayName("Service-7: Submit after deadline sets status LATE")
    void submit_afterDeadline_statusIsLate() {
        // Arrange
        mockAssignment = Assignment.builder()
                .id(1L).title("Assignment 1").course(mockCourse)
                .deadline(LocalDateTime.now().minusDays(1)).build(); // past deadline

        SubmissionRequest request = new SubmissionRequest();
        request.setAssignmentId(1L);
        request.setGithubUrl("https://github.com/student/late");

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(mockStudent));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(mockAssignment));
        when(submissionRepository.existsByStudentIdAndAssignmentId(1L, 1L)).thenReturn(false);
        when(submissionRepository.save(any())).thenAnswer(inv -> {
            Submission s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        // Act
        SubmissionResponse response = submissionService.submit("student1", request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.LATE);
    }

    @Test
    @DisplayName("Service-8: Duplicate submission throws 409 Conflict")
    void submit_duplicate_throwsConflict() {
        // Arrange
        SubmissionRequest request = new SubmissionRequest();
        request.setAssignmentId(1L);

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(mockStudent));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(mockAssignment));
        when(submissionRepository.existsByStudentIdAndAssignmentId(1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submit("student1", request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("Service-9: Grade SUBMITTED submission sets status to GRADED")
    void gradeSubmission_submitted_setsGraded() {
        // Arrange
        Submission submission = Submission.builder()
                .id(1L).student(mockStudent).assignment(mockAssignment)
                .status(SubmissionStatus.SUBMITTED).build();

        GradeRequest request = new GradeRequest();
        request.setSubmissionId(1L);
        request.setScore(85);
        request.setFeedback("Good work!");

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SubmissionResponse response = submissionService.gradeSubmission(request);

        // Assert
        assertThat(response.getScore()).isEqualTo(85);
        assertThat(response.getFeedback()).isEqualTo("Good work!");
        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.GRADED);
        assertThat(response.getGradedAt()).isNotNull();
    }

    @Test
    @DisplayName("Service-10: Grade PENDING submission throws 400 Bad Request")
    void gradeSubmission_pending_throwsBadRequest() {
        // Arrange
        Submission submission = Submission.builder()
                .id(1L).student(mockStudent).assignment(mockAssignment)
                .status(SubmissionStatus.PENDING).build();

        GradeRequest request = new GradeRequest();
        request.setSubmissionId(1L);
        request.setScore(90);

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));

        // Act & Assert
        assertThatThrownBy(() -> submissionService.gradeSubmission(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
