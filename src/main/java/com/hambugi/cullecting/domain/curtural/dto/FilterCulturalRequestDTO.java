package com.hambugi.cullecting.domain.curtural.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterCulturalRequestDTO {
    private String codeName; // 문화행사 분야
    private String guName; // 지역
    private String themeCode; // 연령
    private Boolean isFree; // 유료 무료
    public FilterCulturalRequestDTO(String codeName, String guName, String themeCode, Boolean isFree) {
        this.codeName = codeName;
        this.guName = guName;
        this.themeCode = themeCode;
        this.isFree = isFree;
    }
}
