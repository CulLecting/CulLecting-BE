package com.hambugi.cullecting.domain.archiving.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PreferenceCardDTO {
    private List<String> keywords;
    private int culturalCount;
    private String manyCategory;
    // 취향 카드 나오면 enum 으로 데이터 보내야될듯
}
