package com.example.course.config;

import com.example.course.entity.User;
import com.example.course.enums.Role;
import com.example.course.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("Admin@123"))
                        .email("admin@system.com")
                        .fullName("System Administrator")
                        .role(Role.ADMIN)
                        .active(true)
                        .build();
                userRepository.save(admin);
                log.info("Admin account created: admin / Admin@123");
            }

            if (!userRepository.existsByUsername("lecturer1")) {
                User lecturer = User.builder()
                        .username("lecturer1")
                        .password(passwordEncoder.encode("Lecturer@123"))
                        .email("lecturer1@university.edu")
                        .fullName("Dr. Nguyen Van A")
                        .role(Role.LECTURER)
                        .active(true)
                        .build();
                userRepository.save(lecturer);
                log.info("Lecturer account created: lecturer1 / Lecturer@123");
            }
        };
    }
}
