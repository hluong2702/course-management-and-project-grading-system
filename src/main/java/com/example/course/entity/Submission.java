package com.example.course.entity;

import com.example.course.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    private String githubUrl;

    private String reportUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(updatable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime gradedAt;

    @PrePersist
    public void prePersist() {
        submittedAt = LocalDateTime.now();
    }
}
