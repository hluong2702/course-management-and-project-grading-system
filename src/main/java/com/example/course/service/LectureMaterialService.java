package com.example.course.service;

import com.example.course.dto.response.LectureMaterialResponse;
import com.example.course.dto.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface LectureMaterialService {
    LectureMaterialResponse uploadMaterial(String username, Long courseId, String title, MultipartFile file);
    PageResponse<LectureMaterialResponse> getMaterialsByCourse(Long courseId, int page, int size);
}
