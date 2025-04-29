package com.hambugi.cullecting.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationResponse {
    private String token;
    public VerificationResponse(String token) {
        this.token = token;
    }
}
