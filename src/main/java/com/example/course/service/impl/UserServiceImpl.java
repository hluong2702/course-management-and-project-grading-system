package com.example.course.service.impl;

import com.example.course.dto.request.UserUpdateRequest;
import com.example.course.dto.response.PageResponse;
import com.example.course.dto.response.UserResponse;
import com.example.course.entity.User;
import com.example.course.enums.Role;
import com.example.course.exception.AppException;
import com.example.course.repository.UserRepository;
import com.example.course.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public PageResponse<UserResponse> getAllUsers(int page, int size, Role role, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage;

        if (keyword != null && !keyword.isBlank()) {
            userPage = userRepository.searchByRoleAndKeyword(role, keyword, pageable);
        } else if (role != null) {
            userPage = userRepository.findByRole(role, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserResponse> content = userPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail() != null) {
            if (userRepository.existsByEmail(request.getEmail()) && !user.getEmail().equals(request.getEmail())) {
                throw new AppException("Email đã được sử dụng", HttpStatus.CONFLICT);
            }
            user.setEmail(request.getEmail());
        }
        if (request.getActive() != null) user.setActive(request.getActive());

        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
