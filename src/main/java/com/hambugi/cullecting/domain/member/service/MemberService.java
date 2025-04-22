package com.hambugi.cullecting.domain.member.service;

import com.hambugi.cullecting.domain.member.dto.LoginRequest;
import com.hambugi.cullecting.domain.member.dto.OnboardingRequest;
import com.hambugi.cullecting.domain.member.dto.SignUpRequest;
import com.hambugi.cullecting.domain.member.dto.TokenResponse;
import com.hambugi.cullecting.domain.member.entity.Member;
import com.hambugi.cullecting.domain.member.repository.MemberRepository;
import com.hambugi.cullecting.global.jwt.JwtTokenUtil;
import com.hambugi.cullecting.global.redis.RedisTokenType;
import com.hambugi.cullecting.global.redis.RedisUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MemberService {
    private MemberRepository memberRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private JwtTokenUtil jwtTokenUtil;
    private RedisTemplate<String, String> redisTemplate;
    private RedisUtil redisUtil;

    public MemberService(MemberRepository memberRepository, JwtTokenUtil jwtTokenUtil, RedisTemplate<String, String> redisTemplate , RedisUtil redisUtil) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtTokenUtil = jwtTokenUtil;
        this.redisTemplate = redisTemplate;
        this.redisUtil = redisUtil;
    }

    // 회원가입
    public void signUp(SignUpRequest signUpRequest) {
        Member member = new Member();
        member.setEmail(signUpRequest.getEmail());
        member.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        member.setNickname(signUpRequest.getNickname());
        memberRepository.save(member);
    }

    // 로그인
    public Member login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmail(loginRequest.getEmail());
        if (member == null) {
            return null;
        } else if (passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            return member;
        } else {
            return null;
        }
    }

    // 이메일로 사용자 찾기
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // 이메일이 존재하는지 확인
    public boolean isEmailExist(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 사용자 업데이트
    public void updateMember(Member member) {
        memberRepository.save(member);
    }

    // 온보딩 데이터추가
    public boolean addOnboard(String email, OnboardingRequest onboardingRequest) {
        Member member = findByEmail(email);
        if (member == null) {
            return false;
        }
        member.setCategoryList(onboardingRequest.getCategory());
        member.setLocationList(onboardingRequest.getLocation());
        memberRepository.save(member);
        return true;
    }

    // 닉네임 변경
    public void nicknameUpdate(String email, String nickname) {
        Member member = findByEmail(email);
        if (member == null) {
            return;
        }
        member.setNickname(nickname);
        memberRepository.save(member);
    }

    // 비밀번호 재설정
    public void resetPassword(String email, String password) {
    	Member member = memberRepository.findByEmail(email);
    	member.setPassword(passwordEncoder.encode(password));
        memberRepository.save(member);
    }

    // 비밀번호 확인
    public boolean passwordEquals(String email, String password) {
        Member member = memberRepository.findByEmail(email);
        return passwordEncoder.matches(password, member.getPassword());
    }

    // 토큰 생성
    public TokenResponse generateAccessToken(String email) {
        String accessToken = jwtTokenUtil.generateAccessToken(email);
        String refreshToken = jwtTokenUtil.generateRefreshToken(email);
        TokenResponse response = new TokenResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        redisUtil.saveToken(RedisTokenType.ACCESS_TOKEN, email, accessToken);
        redisUtil.saveToken(RedisTokenType.REFRESH_TOKEN, email, refreshToken);
        return response;
    }

    // 토큰 찾기
    public String findAccessToken(String email) {
        return redisUtil.getToken(RedisTokenType.ACCESS_TOKEN, email);
    }

    public String findRefreshToken(String email) {
        return redisUtil.getToken(RedisTokenType.REFRESH_TOKEN, email);
    }

    // 토큰 값 비교
    public boolean validateRedisToken(String email, String token, RedisTokenType redisTokenType) {
        token = jwtTokenUtil.getPureToken(token);
        return redisUtil.validateToken(redisTokenType, email, token);
    }

    // 유효한 토큰인지 확인
    public boolean validateToken(String token) {
        token = jwtTokenUtil.getPureToken(token);
        return jwtTokenUtil.validateToken(token);
    }

    public boolean validateTokenType(String token, RedisTokenType type) {
        token = jwtTokenUtil.getPureToken(token);
        return jwtTokenUtil.getTokenType(token).equals(type.name());
    }

    public boolean validateSignUpEmail(String token, String email) {
        token = jwtTokenUtil.getPureToken(token);
        return jwtTokenUtil.getEmailFromToken(token).equals(email);
    }

    public String findEmailByToken(String token) {
        token = jwtTokenUtil.getPureToken(token);
        return jwtTokenUtil.getEmailFromToken(token);
    }

    // 토큰 삭제
    public void removeMemberToken(String email) {
        redisUtil.deleteToken(RedisTokenType.ACCESS_TOKEN, email);
        redisUtil.deleteToken(RedisTokenType.REFRESH_TOKEN, email);
    }

    // 사용자 삭제
    public void removeMember(String email) {
        removeMemberToken(email);
        memberRepository.delete(findByEmail(email));
    }

}
