package com.hambugi.cullecting.domain.member.controller;

import com.hambugi.cullecting.domain.mail.service.MailService;
import com.hambugi.cullecting.domain.member.dto.*;
import com.hambugi.cullecting.domain.member.entity.Member;
import com.hambugi.cullecting.domain.member.service.MemberService;
import com.hambugi.cullecting.global.jwt.JwtTokenUtil;
import com.hambugi.cullecting.global.redis.RedisTokenType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenUtil jwtTokenUtil;
    private final MailService mailService;

    public MemberController(MemberService memberService, JwtTokenUtil jwtTokenUtil, MailService mailService) {
        this.memberService = memberService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.mailService = mailService;
    }

    // 이메일 인증 메일 보내기
    @PostMapping("/email-verifications")
    public ResponseEntity<?> send(@RequestBody EmailRequest request) {
        log.info("Email={} : 인증 메일 보내기 요청", request.getEmail());
        if (!sendEmail(request, true)) {
            log.warn("Email={} : 이미 사용중인 이메일", request.getEmail());
            return ResponseEntity.status(409).body(ApiResponse.error(409, "이미 등록된 이메일입니다."));
        }
        log.info("Email={} : 인증 메일 보내기 성공", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("이메일 인증번호 전송 성공", null));
    }

    // 인증번호 확인하기
    @PostMapping("/email-verifications/verify")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyRequest request) {
        log.info("Email={} : 인증번호 확인하기 요청", request.getEmail());
        boolean result = mailService.verifyCode(request.getEmail(), request.getCode());
        if (!result) {
            log.info("Email={} : 인증번호가 일치하지 않거나 인증 시간 만료", request.getEmail());
            return ResponseEntity.status(401).body(ApiResponse.error(401, "인증번호가 일치하지 않거나 인증시간이 만료되었습니다."));
        }
        String token = jwtTokenUtil.generateSignUpToken(request.getEmail());
        log.info("Email={} : 인증 완료", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("이메일 인증 성공", new VerificationResponse(token)));
    }

    // 회원가입 진행하기
    @PostMapping
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request, @RequestHeader("Authorization") String token) {
        log.info("Email={} : 회원가입 요청", request.getEmail());
        if (!memberService.validateToken(token)) {
            log.warn("Email={} : 유효하지 않은 토큰", request.getEmail());
            return ResponseEntity.status(401).body(ApiResponse.error(401, "유효하지 않은 토큰입니다."));
        }
        if (!memberService.validateTokenType(token, RedisTokenType.EMAIL_VERIFICATION)) {
            log.warn("Email={} : 이상한 토큰 유형", request.getEmail());
            return ResponseEntity.status(403).body(ApiResponse.error(403, "토큰 유형이 이메일 인증용이 아닙니다."));
        }
        if (!memberService.validateSignUpEmail(token, request.getEmail())) {
            log.warn("Email={} : 인증정보 불일치", request.getEmail());
            return ResponseEntity.status(403).body(ApiResponse.error(403, "이메일 인증 정보가 일치하지 않습니다."));
        }
        if (memberService.isEmailExist(request.getEmail())) {
            log.warn("Email={} : 이미 등록된 이메일", request.getEmail());
            return ResponseEntity.status(409).body(ApiResponse.error(409, "이미 등록된 이메일입니다."));
        }
        memberService.signUp(request);
        log.info("Email={} : 회원가입 성공", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("회원가입 완료", null));
    }

    // 로그인 진행하기
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Email={} : 로그인 요청", loginRequest.getEmail());
        Member member = memberService.login(loginRequest);
        if (member == null) {
            log.warn("Email={} : 아이디 혹은 비밀번호 불일치", loginRequest.getEmail());
            return ResponseEntity.status(401).body(ApiResponse.error(401, "아이디 또는 비밀번호가 올바르지 않습니다."));
        }
        TokenResponse response = memberService.generateAccessToken(member.getEmail());
        log.info("Email={} : 로그인 성공 및 토큰 발급 성공", member.getEmail());
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }

    // 유저정보 가져오기
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Email={} : 사용자 정보 요청", userDetails.getUsername());
        MemberResponseDTO member = memberService.findByMemberResponseFromEmail(userDetails.getUsername());
        if (member == null) {
            log.warn("Email={} : 사용자 정보 없음", userDetails.getUsername());
            return ResponseEntity.status(404).body(ApiResponse.error(404, "회원 정보를 찾을 수 없습니다."));
        }
        log.info("Email={} : 사용자 정보 조회 성공", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회 성공", member));
    }

    // 만료된 accessToken 을 재발급 받기 위함
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        log.info("RefreshToken 재발급 요청");
        if (!memberService.validateToken(token)) {
            log.warn("유효하지 않은 토큰");
            return ResponseEntity.status(401).body(ApiResponse.error(401, "유효하지 않은 토큰입니다."));
        }
        if (!memberService.validateTokenType(token, RedisTokenType.REFRESH_TOKEN)) {
            log.warn("유효하지 않은 토큰 타입");
            return ResponseEntity.status(403).body(ApiResponse.error(403, "토큰 유형이 리프레시 토큰이 아닙니다."));
        }
        String email = memberService.findEmailByToken(token);
        if (!memberService.validateRedisToken(email, token, RedisTokenType.REFRESH_TOKEN)) {
            log.warn("토큰 정보 불일치");
            return ResponseEntity.status(403).body(ApiResponse.error(403, "토큰 정보가 일치하지 않습니다."));
        }
        TokenResponse response = memberService.generateAccessToken(email);
        log.info("Email={} : 토큰 재발급 성공", email);
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공", response));
    }

    // 로그인 페이지에서 비밀번호 재발급 메일 보내기
    @PostMapping("/password/reset-request")
    public ResponseEntity<?> resetPassword(@RequestBody EmailRequest emailRequest) {
        log.info("Email={} : 로그인 페이지에서 비밀번호 재발급 메일 요청", emailRequest.getEmail());
        if (!sendEmail(emailRequest, false)) {
            log.warn("Email={} : 사용자가 존재하지 않음", emailRequest.getEmail());
            return ResponseEntity.status(404).body(ApiResponse.error(404, "해당 이메일이 존재하지 않습니다."));
        }
        log.info("Email={} : 비밀번호 재발급 메일 전송 성공" , emailRequest.getEmail());
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 인증번호 전송 성공", null));
    }

    // 로그인 페이지 비밀번호 업데이트
    @PatchMapping("/password")
    public ResponseEntity<?> passwordUpdate(@RequestBody PasswordRequest request, @RequestHeader("Authorization") String token) {
        log.info("로그인 페이지에서 비밀번호 업데이트 요청");
        if (!memberService.validateToken(token)) {
            log.warn("유효하지 않은 토큰");
            return ResponseEntity.status(401).body(ApiResponse.error(401, "유효하지 않은 토큰입니다."));
        }
        if (!memberService.validateTokenType(token, RedisTokenType.EMAIL_VERIFICATION)) {
            log.warn("유효하지 않은 토큰 타입");
            return ResponseEntity.status(403).body(ApiResponse.error(403, "이메일 인증용 토큰이 아닙니다."));
        }
        if (!memberService.validateSignUpEmail(token, request.getEmail())) {
            log.warn("인증정보 불일치");
            return ResponseEntity.status(403).body(ApiResponse.error(403, "이메일 인증 정보가 일치하지 않습니다."));
        }
        if (!memberService.isEmailExist(request.getEmail())) {
            log.warn("Email={} : 등록되지 않은 이메일", request.getEmail());
            return ResponseEntity.status(404).body(ApiResponse.error(404, "등록되지 않은 이메일입니다."));
        }
        memberService.resetPassword(request.getEmail(), request.getNewPassword());
        log.info("Email={} : 비밀번호 업데이트 성공", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 완료", null));
    }

    // 닉네임 업데이트
    @PatchMapping("/nickname")
    public ResponseEntity<?> nicknameUpdate(@RequestBody NicknameUpdateRequest nickname, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Email={} : 닉네임 업데이트 요청", userDetails.getUsername());
        memberService.nicknameUpdate(userDetails.getUsername(), nickname.getNickname());
        return ResponseEntity.ok(ApiResponse.success("닉네임 변경 완료", null));
    }

    // 온보딩 데이터 추가
    @PostMapping("/onboarding")
    public ResponseEntity<?> onboarding(@AuthenticationPrincipal UserDetails userDetails, @RequestBody OnboardingRequest onboardingRequest) {
        log.info("Email={} : 온보딩 데이터 추가 요청", userDetails.getUsername());
        boolean result = memberService.addOnboard(userDetails.getUsername(), onboardingRequest);
        if (!result) {
            log.warn("Email={} : 온보딩 데이터 추가 실패", userDetails.getUsername());
            return ResponseEntity.status(500).body(ApiResponse.error(500, "온보딩 정보 저장 실패"));
        }
        log.info("Email={} : 온보딩 데이터 추가 성공", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("온보딩 정보 저장 완료", null));
    }

    // 마이페이지에서 비밀번호 변경하기
    @PatchMapping("/mypage/password")
    public ResponseEntity<?> passwordReset(@AuthenticationPrincipal UserDetails userDetails, @RequestBody PasswordResetRequest resetRequest) {
        log.info("Email={} : 마이페이지에서 비밀번호 수정", userDetails.getUsername());
        if(!memberService.passwordEquals(userDetails.getUsername(), resetRequest.getBeforePassword())) {
            log.warn("Email={} : 기존 비밀번호 불일치", userDetails.getUsername());
            return ResponseEntity.status(403).body(ApiResponse.error(403, "기존 비밀번호와 일치하지 않음"));
        }
        memberService.resetPassword(userDetails.getUsername(), resetRequest.getNewPassword());
        log.info("Email={} : 비밀번호 수정 성공", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 완료", null));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Email={} : 로그아웃 요청", userDetails.getUsername());
        memberService.removeMemberToken(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공", null));
    }

    // 회원 탈퇴
    @DeleteMapping
    public ResponseEntity<?> deleteMember(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Email={} : 회원 탈퇴 요청", userDetails.getUsername());
        memberService.removeMember(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴 완료", null));
    }

    // 메일 보내기 기능을 하나로
    public boolean sendEmail(EmailRequest emailRequest, boolean isFirst) {
        if (isFirst) {
            if (memberService.isEmailExist(emailRequest.getEmail())) {
                return false;
            }
        } else {
            if (!memberService.isEmailExist(emailRequest.getEmail())) {
                return false;
            }
        }
        mailService.sendVerificationCode(emailRequest.getEmail());
        return true;
    }

}
