package com.example.course.repository;

import com.example.course.entity.LectureMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureMaterialRepository extends JpaRepository<LectureMaterial, Long> {
    Page<LectureMaterial> findByCourseId(Long courseId, Pageable pageable);
}
