package com.hambugi.cullecting.domain.mail.repository;

import com.hambugi.cullecting.global.redis.RedisTokenType;
import com.hambugi.cullecting.global.redis.RedisUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MailRepository {

    private final RedisUtil redisUtil;

    public MailRepository(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    // 인증 코드가 맞는지 확인
    public boolean verifyCode(String email, String inputCode) {
        String storedCode = redisUtil.getToken(RedisTokenType.EMAIL_VERIFICATION, email);
        boolean result = storedCode != null && storedCode.equals(inputCode);
        if (result) {
            redisUtil.deleteToken(RedisTokenType.EMAIL_VERIFICATION, email);
        }
        return result;
    }

    // 인증코드 redis 데이터베이스에 저장
    public void setCode(String email, String code) {
        redisUtil.saveToken(RedisTokenType.EMAIL_VERIFICATION, email, code);
    }
}
