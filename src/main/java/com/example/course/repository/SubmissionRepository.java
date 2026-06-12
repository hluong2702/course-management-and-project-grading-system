package com.example.course.repository;

import com.example.course.entity.Submission;
import com.example.course.enums.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    boolean existsByStudentIdAndAssignmentId(Long studentId, Long assignmentId);
    Optional<Submission> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);
    Page<Submission> findByAssignmentId(Long assignmentId, Pageable pageable);
    Page<Submission> findByStudentId(Long studentId, Pageable pageable);
    Page<Submission> findByAssignmentIdAndStatus(Long assignmentId, SubmissionStatus status, Pageable pageable);
}
