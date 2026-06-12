package com.example.course.service;

import com.example.course.dto.request.GradeRequest;
import com.example.course.dto.request.SubmissionRequest;
import com.example.course.dto.response.PageResponse;
import com.example.course.dto.response.SubmissionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SubmissionService {
    SubmissionResponse submit(String username, SubmissionRequest request);
    SubmissionResponse uploadReport(String username, Long assignmentId, MultipartFile file);
    SubmissionResponse gradeSubmission(GradeRequest request);
    PageResponse<SubmissionResponse> getSubmissionsByAssignment(Long assignmentId, int page, int size);
    PageResponse<SubmissionResponse> getMySubmissions(String username, int page, int size);
}
