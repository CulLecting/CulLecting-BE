package com.hambugi.cullecting.global.redis;

import lombok.Getter;

import java.time.Duration;

public enum RedisTokenType {
    EMAIL_VERIFICATION("email", Duration.ofMinutes(3)),
    ACCESS_TOKEN("access", Duration.ofDays(7)),
    REFRESH_TOKEN("refresh", Duration.ofDays(30));
    private final String prefix;
    @Getter
    private final Duration duration;

    RedisTokenType(String prefix, Duration duration) {
        this.prefix = prefix;
        this.duration = duration;
    }

    public String getKey(String email) {
        return prefix + ":" + email;
    }

    public String getTokenTypeClaim() {
        return this.name(); // JWT 클레임용: "EMAIL_VERIFICATION", "ACCESS", "REFRESH"
    }

}
