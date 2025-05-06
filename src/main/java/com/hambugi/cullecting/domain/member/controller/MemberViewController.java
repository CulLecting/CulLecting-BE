package com.hambugi.cullecting.domain.member.controller;

import ch.qos.logback.core.model.Model;
import com.hambugi.cullecting.domain.member.dto.EmailRequest;
import com.hambugi.cullecting.domain.member.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/member/view")
public class MemberViewController {
    private final MemberService memberService;
    public MemberViewController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/delete-form")
    public String deleteForm() {
        return "MemberDelete";
    }

    @PostMapping("/delete")
    public String delete(@RequestBody EmailRequest emailRequest) {
        System.out.println(emailRequest.getEmail());
        memberService.removeMember(emailRequest.getEmail());
        return "redirect:/member/view";
    }

}
