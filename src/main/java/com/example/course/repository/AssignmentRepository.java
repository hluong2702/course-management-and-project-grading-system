package com.example.course.repository;

import com.example.course.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Page<Assignment> findByCourseId(Long courseId, Pageable pageable);
}
