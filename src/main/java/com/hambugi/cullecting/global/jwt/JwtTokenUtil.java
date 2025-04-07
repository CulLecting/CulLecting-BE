package com.hambugi.cullecting.global.jwt;

import com.hambugi.cullecting.global.redis.RedisTokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenUtil {
    private final SecretKey secretKey;
    private final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7;   // 7일
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 30; // 30일
    private final long SIGN_UP_TOKEN_EXPIRATION = 1000L * 60 * 60; // 1시간
    private final String TOKEN_PREFIX = "Bearer ";

    public JwtTokenUtil() {
        String key = "L9v2mKqp3E8hYc4SxN7WgRjD5zUqVt0pBmAeCrXsYzTnMkJfQwLbEiHgZdRuTsAv";
        this.secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String email) {
        return generateToken(email, RedisTokenType.ACCESS_TOKEN);
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, RedisTokenType.REFRESH_TOKEN);
    }

    public String generateSignUpToken(String email) {
        return generateToken(email, RedisTokenType.EMAIL_VERIFICATION);
    }

//    private String generateToken(String subject, long expirationMillis) {
//        Date now = new Date();
//        Date expiry = new Date(now.getTime() + expirationMillis);
//
//        return Jwts.builder()
//                .setSubject(subject)
//                .setIssuedAt(now)
//                .setExpiration(expiry)
//                .signWith(secretKey, SignatureAlgorithm.HS512)
//                .compact();
//    }

//    private String generateToken(String subject, long expirationMillis, String tokenType) {
//        Date now = new Date();
//        Date expiry = new Date(now.getTime() + expirationMillis);
//
//        return Jwts.builder()
//                .setSubject(subject)
//                .claim("tokenType", tokenType)
//                .setIssuedAt(now)
//                .setExpiration(expiry)
//                .signWith(secretKey, SignatureAlgorithm.HS512)
//                .compact();
//    }

    public String generateToken(String email, RedisTokenType type) {
        Instant now = Instant.now();
        Instant expiry = now.plus(type.getDuration());
        return Jwts.builder()
                .setSubject(email)
                .claim("tokenType", type.getTokenTypeClaim()) // 👈 타입 클레임 추가
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    // pureToken 으로 수정
    public String getPureToken(String token) {
        return token.replace(TOKEN_PREFIX, "");
    }

    public String getTokenType(String token) {
        return getAllClaimsFromToken(token).get("tokenType", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

//    // 토큰 확인
//    public boolean validateToken(String token) {
//        try {
//            getParser().parseClaimsJws(getPureToken(token));
//            return true;
//        } catch (JwtException | IllegalArgumentException e) {
//            e.printStackTrace(); // 로그 남기기
//            return false;
//        }
//    }

    // 토큰에서 이메일 확인
    public String getEmailFromToken(String token) {
        return getAllClaimsFromToken(getPureToken(token)).getSubject();
    }

    // 토큰에서 만료시간 확인
    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(getPureToken(token)).getExpiration();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isEmailVerificationToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return "EMAIL_VERIFICATION".equals(claims.get("tokenType"));
    }

    private JwtParser getParser() {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build();
    }

}
