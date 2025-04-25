package com.hambugi.cullecting.global.jwt;

import com.hambugi.cullecting.domain.member.service.CustomUserDetailsService;
import com.hambugi.cullecting.global.redis.RedisTokenType;
import com.hambugi.cullecting.global.redis.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final PathMatcher pathMatcher;
    private JwtTokenUtil jwtTokenUtil;
    private CustomUserDetailsService userDetailsService;
    private RedisUtil redisUtil;
    private final List<String> EXCLUDE_URLS = List.of(
            "/member/send",
            "/member/verify",
            "/member/login",
            "/images/**",
            "/cultural/findculturalimage",
            "/cultural/findculturalfromdate",
            "/cultural/culturalfilter",
            "/cultural/findculturalname",
            "/cultural/latestcultural",
            "/cultural/culturaldetail"
    );

    public JwtRequestFilter(JwtTokenUtil jwtTokenUtil, CustomUserDetailsService userDetailsService, RedisUtil redisUtil, PathMatcher pathMatcher) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.redisUtil = redisUtil;
        this.pathMatcher = pathMatcher;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            String requestURI = request.getRequestURI();
            boolean isExcluded = EXCLUDE_URLS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestURI));

            if (isExcluded) {
                chain.doFilter(request, response);
                return;
            }
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization 헤더가 필요합니다.");
            return;
        }
        String token = jwtTokenUtil.getPureToken(header);
        String email;
        try {
            email = jwtTokenUtil.getEmailFromToken(token);
        } catch (ExpiredJwtException e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다.");
            return;
        } catch (Exception e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }
        String tokenType = jwtTokenUtil.getTokenType(token);
        try {
            RedisTokenType type = RedisTokenType.valueOf(tokenType);

            switch (type) {
                case EMAIL_VERIFICATION -> {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                case ACCESS_TOKEN -> {
                    if (!redisUtil.validateToken(RedisTokenType.ACCESS_TOKEN, email, token)) {
                        setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Redis 저장 토큰과 일치하지 않습니다.");
                        return;
                    }
                    System.out.println(111);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    System.out.println(userDetails.getUsername());
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                case REFRESH_TOKEN -> {
                    if (!redisUtil.validateToken(RedisTokenType.REFRESH_TOKEN, email, token)) {
                        setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Redis 저장 토큰과 일치하지 않습니다.");
                        return;
                    }
                    String path = request.getRequestURI();
                    if ("/member/refreshtoken".equals(path)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        System.out.println(userDetails.getUsername());
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        setErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "리프레시 토큰은 인증에 사용할 수 없습니다.");
                        return;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "알 수 없는 토큰 타입입니다.");
            return;
        }
        chain.doFilter(request, response);
    }

    private void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

}
