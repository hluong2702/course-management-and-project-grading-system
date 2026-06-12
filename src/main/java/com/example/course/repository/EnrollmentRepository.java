package com.example.course.repository;

import com.example.course.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);
    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);
}
