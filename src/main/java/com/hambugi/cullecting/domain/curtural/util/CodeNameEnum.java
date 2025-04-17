package com.hambugi.cullecting.domain.curtural.util;

import java.util.List;

public enum CodeNameEnum {
    // 최신행사 (전체, 카테고리에 맞게)
    /*
    공연/예술(뮤지컬/오페라, 무용, 연극, 영화)
    음악(클래식, 국악, 콘서트, 독주/독창회)
    전시/미술(전시/미술)
    축제/야외체험(축제-시민화합, 축제-전통/역사, 축제-기타, 축제-자연/경관)
    문화/예술 일반(축제-문화/예술)
    교육/체험(교육/체험)
    기타(기타)
    */
    PERFORMANCE("공연/예술", List.of("뮤지컬/오페라", "무용", "연극", "영화")),
    MUSIC("음악", List.of("클래식", "국악", "콘서트", "독주/독창회")),
    EXHIBITION("전시/미술", List.of("전시/미술")),
    FESTIVAL("축제/야외체험", List.of("축제-시민화합", "축제-전통/역사", "축제-기타", "축제-자연/경관")),
    CULTURAL("문화/예술", List.of("축제-문화/예술")),
    EDUCATION("교육/체험", List.of("교육/체험")),
    OTHER("기타", List.of("기타"));

    private final String label;
    private final List<String> data;

    CodeNameEnum(String label, List<String> data) {
        this.label = label;
        this.data = data;
    }

    public String getLabel() {
        return label;
    }
    public List<String> getData() {
        return data;
    }
}
