package com.example.course.repository;

import com.example.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByCode(String code);
    Page<Course> findByActiveTrue(Pageable pageable);
    @Query("SELECT c FROM Course c WHERE c.active = true AND (c.name LIKE %:keyword% OR c.code LIKE %:keyword%)")
    Page<Course> searchByKeyword(String keyword, Pageable pageable);
}
