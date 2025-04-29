package com.hambugi.cullecting.domain.member.controller;

import com.hambugi.cullecting.domain.mail.service.MailService;
import com.hambugi.cullecting.domain.member.dto.*;
import com.hambugi.cullecting.domain.member.entity.Member;
import com.hambugi.cullecting.domain.member.service.MemberService;
import com.hambugi.cullecting.global.jwt.JwtTokenUtil;
import com.hambugi.cullecting.global.redis.RedisTokenType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody EmailRequest emailRequest) {
        boolean result = sendEmail(emailRequest, true);
        if (!result) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "이미 등록된 이메일 입니다."));
        }
        return ResponseEntity.ok(ApiResponse.success("인증번호 전송 완료", null));
    }

    // 인증번호 확인하기
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyRequest request) {
        boolean result = mailService.verifyCode(request.getEmail(), request.getCode());
        VerificationResponse response = new VerificationResponse();
        if (result) {
            String token = jwtTokenUtil.generateSignUpToken(request.getEmail());
            response.setToken(token);
            return ResponseEntity.ok(ApiResponse.success("인증 성공", response));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(403, "인증번호 불일치 또는 만료됨"));
        }
    }

    // 회원가입 진행하기
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest signUpRequest, @RequestHeader("Authorization") String token) {
        if (!memberService.validateToken(token)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403,"유효하지 않은 토큰") );
        }
        if (!memberService.validateTokenType(token, RedisTokenType.EMAIL_VERIFICATION)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "유효하지 않은 토큰 타입"));
        }
        if (!memberService.validateSignUpEmail(token, signUpRequest.getEmail())) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "인증 이메일과 입력한 이메일이 다름"));
        }
        if (memberService.isEmailExist(signUpRequest.getEmail())) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "이미 등록된 이메일 입니다."));
        } else {
            memberService.signUp(signUpRequest);
            if (!memberService.isEmailExist(signUpRequest.getEmail())) {
                return ResponseEntity.status(500).body(ApiResponse.error(500, "회원가입 실패"));
            } else {
                return ResponseEntity.ok(ApiResponse.success("회원가입 완료", null));
            }
        }
    }

    // 로그인 진행하기
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Member member = memberService.login(loginRequest);
        if (member == null) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "아이디 혹은 비밀번호가 잘못되었습니다."));
        }
        TokenResponse response = memberService.generateAccessToken(member.getEmail());
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }

    // 유저정보 가져오기
    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        MemberResponseDTO member = memberService.findByMemberResponseFromEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("유저정보 찾기 성공", member));
    }

    // 만료된 accessToken 을 재발급 받기 위함
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        if (!memberService.validateToken(token)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "유효하지 않은 토큰"));
        }
        if (!memberService.validateTokenType(token, RedisTokenType.REFRESH_TOKEN)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "유효하지 않은 토큰 타입"));
        }
        String email = memberService.findEmailByToken(token);
        if (!memberService.validateRedisToken(email, token, RedisTokenType.REFRESH_TOKEN)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "저장된 리프레쉬 토큰과 일치하지 않습니다."));
        }
        TokenResponse response = memberService.generateAccessToken(email);
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공", response));
    }

    // 로그인 페이지에서 비밀번호 재발급 메일 보내기
    @PostMapping("/login/resetpassword")
    public ResponseEntity<?> resetPassword(@RequestBody EmailRequest emailRequest) {
        boolean result = sendEmail(emailRequest, false);
        if (!result) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "존재하지 않는 이메일"));
        }
        return ResponseEntity.ok(ApiResponse.success("인증번호 전송 완료", null));
    }

    // 로그인 페이지 비밀번호 업데이트
    @PostMapping("/passwordupdate")
    public ResponseEntity<?> passwordUpdate(@RequestBody PasswordRequest passwordRequest, @RequestHeader("Authorization") String token) {
        if (!memberService.validateToken(token)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "유효하지 않은 토큰"));
        }
        if (!memberService.validateTokenType(token, RedisTokenType.EMAIL_VERIFICATION)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "유효하지 않은 토큰 타입"));
        }
        if (!memberService.validateSignUpEmail(token, passwordRequest.getEmail())) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "인증 이메일과 입력한 이메일이 다름"));
        }
        if (!memberService.isEmailExist(passwordRequest.getEmail())) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "등록되지 않은 이메일 입니다."));
        } else {
            memberService.resetPassword(passwordRequest.getEmail(), passwordRequest.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("비밀번호 변경완료", null));
        }
    }

    // 닉네임 업데이트
    @PostMapping("/nicknameupdate")
    public ResponseEntity<?> nicknameUpdate(@RequestBody NicknameUpdateRequest nickname, @AuthenticationPrincipal UserDetails userDetails) {
        memberService.nicknameUpdate(userDetails.getUsername(), nickname.getNickname());
        return ResponseEntity.ok(ApiResponse.success("닉네임 변경 완료", null));
    }

    // 온보딩 데이터 추가
    @PostMapping("/onboarding")
    public ResponseEntity<?> onboarding(@AuthenticationPrincipal UserDetails userDetails, @RequestBody OnboardingRequest onboardingRequest) {
        boolean result = memberService.addOnboard(userDetails.getUsername(), onboardingRequest);
        if (!result) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "추가 실패"));
        }
        return ResponseEntity.ok(ApiResponse.success("추가 완료", null));
    }

    // 마이페이지에서 비밀번호 변경하기
    @PostMapping("/mypage/passwordreset")
    public ResponseEntity<?> passwordReset(@AuthenticationPrincipal UserDetails userDetails, @RequestBody PasswordResetRequest resetRequest) {
        if(!memberService.passwordEquals(userDetails.getUsername(), resetRequest.getBeforePassword())) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "기존 비밀번호와 일치하지 않음"));
        }
        memberService.resetPassword(userDetails.getUsername(), resetRequest.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 완료", null));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails) {
        memberService.removeMemberToken(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공", null));
    }

    // 회원 탈퇴
    @PostMapping("/deletemember")
    public ResponseEntity<?> deleteMember(@AuthenticationPrincipal UserDetails userDetails) {
        memberService.removeMember(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("유저 삭제 성공", null));
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
