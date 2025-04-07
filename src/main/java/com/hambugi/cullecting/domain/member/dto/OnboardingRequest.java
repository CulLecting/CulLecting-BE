package com.hambugi.cullecting.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OnboardingRequest {
    private List<String> location;
    private List<String> category;
}
