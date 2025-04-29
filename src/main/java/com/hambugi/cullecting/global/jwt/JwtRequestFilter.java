package com.hambugi.cullecting.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hambugi.cullecting.domain.member.dto.ApiResponse;
import com.hambugi.cullecting.domain.member.service.CustomUserDetailsService;
import com.hambugi.cullecting.global.redis.RedisTokenType;
import com.hambugi.cullecting.global.redis.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final RedisUtil redisUtil;
    private final List<String> EXCLUDE_URLS = List.of(
            "/member/email-verifications",
            "/member/email-verifications/verify",
            "/member/login",
            "/images/**",
            "/cultural/images",
            "/cultural/date",
            "/cultural/filter",
            "/cultural/search",
            "/cultural/latest"
    );

    public JwtRequestFilter(JwtTokenUtil jwtTokenUtil, CustomUserDetailsService userDetailsService, RedisUtil redisUtil, PathMatcher pathMatcher) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.redisUtil = redisUtil;
        this.pathMatcher = pathMatcher;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            boolean isExcluded = EXCLUDE_URLS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestURI));

            boolean isCulturalIdRequest = requestURI.matches("^/cultural/\\d+$");
            System.out.println("isExcluded: " + isExcluded);
            System.out.println("isCulturalIdRequest: " + isCulturalIdRequest);
            if (isExcluded || isCulturalIdRequest) {
                chain.doFilter(request, response);
                return;
            }
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization 헤더가 필요합니다.");
            return;
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
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

        String tokenType = jwtTokenUtil.getTokenType(token);
        try {
            RedisTokenType type = RedisTokenType.valueOf(tokenType);

            switch (type) {
                case EMAIL_VERIFICATION -> {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, List.of());
                    SecurityContextHolder.clearContext();
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                case ACCESS_TOKEN -> {
                    if (!redisUtil.validateToken(RedisTokenType.ACCESS_TOKEN, email, token)) {
                        setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Redis 저장 토큰과 일치하지 않습니다.");
                        return;
                    }
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    System.out.println(userDetails.getUsername());
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.clearContext();
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                case REFRESH_TOKEN -> {
                    if (!redisUtil.validateToken(RedisTokenType.REFRESH_TOKEN, email, token)) {
                        setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Redis 저장 토큰과 일치하지 않습니다.");
                        return;
                    }
                    if ("/member/token/refresh".equals(requestURI)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.clearContext();
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[디버그] authentication: " + authentication);
        System.out.println("[디버그] isAuthenticated: " + authentication.isAuthenticated());
        System.out.println("[디버그] principal: " + authentication.getPrincipal());
        System.out.println("[디버그] authorities: " + authentication.getAuthorities());
        chain.doFilter(request, response);
    }

    private void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Object> errorResponse = ApiResponse.error(status, message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }

}
