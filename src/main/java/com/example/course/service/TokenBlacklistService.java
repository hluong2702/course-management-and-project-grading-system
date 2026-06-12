package com.example.course.service;

public interface TokenBlacklistService {
    void blacklist(String token, long expirationMs);
    boolean isBlacklisted(String token);
}
