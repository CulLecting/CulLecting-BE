package com.hambugi.cullecting.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {
    private String beforePassword;
    private String newPassword;
}
