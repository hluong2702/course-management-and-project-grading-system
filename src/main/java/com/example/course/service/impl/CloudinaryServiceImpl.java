package com.example.course.service.impl;

import com.cloudinary.Cloudinary;
import com.example.course.exception.AppException;
import com.example.course.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String publicId = folder + "/" + UUID.randomUUID();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "public_id", publicId,
                            "resource_type", "auto",
                            "folder", folder
                    )
            );
            String url = (String) result.get("secure_url");
            log.info("File uploaded to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Cloudinary upload error: {}", e.getMessage());
            throw new AppException("Failed to upload file to cloud storage", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
