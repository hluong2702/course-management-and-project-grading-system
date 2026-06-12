package com.example.course.service.impl;

import com.example.course.dto.response.LectureMaterialResponse;
import com.example.course.dto.response.PageResponse;
import com.example.course.entity.Course;
import com.example.course.entity.LectureMaterial;
import com.example.course.entity.User;
import com.example.course.exception.AppException;
import com.example.course.repository.CourseRepository;
import com.example.course.repository.LectureMaterialRepository;
import com.example.course.repository.UserRepository;
import com.example.course.service.CloudinaryService;
import com.example.course.service.LectureMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureMaterialServiceImpl implements LectureMaterialService {

    private final LectureMaterialRepository materialRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public LectureMaterialResponse uploadMaterial(String username, Long courseId, String title, MultipartFile file) {
        User lecturer = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("Khóa học không tồn tại", HttpStatus.NOT_FOUND));

        String fileUrl = cloudinaryService.uploadFile(file, "materials");

        LectureMaterial material = LectureMaterial.builder()
                .title(title).fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .course(course).lecturer(lecturer).build();

        materialRepository.save(material);
        return mapToResponse(material);
    }

    @Override
    public PageResponse<LectureMaterialResponse> getMaterialsByCourse(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        Page<LectureMaterial> pg = materialRepository.findByCourseId(courseId, pageable);
        List<LectureMaterialResponse> content = pg.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        return PageResponse.<LectureMaterialResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(pg.getTotalElements()).totalPages(pg.getTotalPages()).last(pg.isLast()).build();
    }

    private LectureMaterialResponse mapToResponse(LectureMaterial m) {
        return LectureMaterialResponse.builder()
                .id(m.getId()).title(m.getTitle()).fileUrl(m.getFileUrl())
                .fileName(m.getFileName()).courseId(m.getCourse().getId())
                .courseName(m.getCourse().getName()).uploadedAt(m.getUploadedAt()).build();
    }
}
