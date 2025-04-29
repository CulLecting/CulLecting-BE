package com.hambugi.cullecting.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberResponseDTO {
    private String id;
    private String email;
    private String nickname;
    private List<String> location;
    private List<String> category;
}
