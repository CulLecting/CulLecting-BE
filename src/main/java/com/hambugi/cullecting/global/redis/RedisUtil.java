package com.hambugi.cullecting.global.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    private RedisTemplate<String, String> redisTemplate;

    RedisUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 저장
    public void saveToken(RedisTokenType type, String email, String token) {
        redisTemplate.opsForValue().set(
                type.getKey(email),
                token,
                type.getDuration()
        );
    }

    // 조회
    public String getToken(RedisTokenType type, String email) {
        return redisTemplate.opsForValue().get(type.getKey(email));
    }

    // 삭제
    public void deleteToken(RedisTokenType type, String email) {
        redisTemplate.delete(type.getKey(email));
    }

    // 존재 여부 확인 (옵션)
    public boolean hasToken(RedisTokenType type, String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(type.getKey(email)));
    }

    public boolean validateToken(RedisTokenType type, String email, String token) {
        String stored = getToken(type, email);
        return stored != null && stored.equals(token);
    }

}
