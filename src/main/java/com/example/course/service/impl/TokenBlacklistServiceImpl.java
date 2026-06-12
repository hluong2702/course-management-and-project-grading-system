package com.example.course.service.impl;

import com.example.course.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private final StringRedisTemplate redisTemplate;

    @Override
    public void blacklist(String token, long expirationMs) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "revoked", expirationMs, TimeUnit.MILLISECONDS);
        log.info("Token đã được đưa vào danh sách đen trên Redis với thời gian sống {} ms", expirationMs);
    }

    @Override
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
