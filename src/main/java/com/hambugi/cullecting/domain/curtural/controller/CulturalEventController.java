package com.hambugi.cullecting.domain.curtural.controller;

import com.hambugi.cullecting.domain.curtural.dto.*;
import com.hambugi.cullecting.domain.curtural.entity.CulturalEvent;
import com.hambugi.cullecting.domain.curtural.service.CulturalEventService;
import com.hambugi.cullecting.domain.member.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cultural")
public class CulturalEventController {
    private final CulturalEventService culturalEventService;

    public CulturalEventController(CulturalEventService culturalEventService) {
        this.culturalEventService = culturalEventService;
    }

    // 문화 이미지 검색
    @GetMapping("/findculturalimage")
    public ResponseEntity<?> findCulturalImage(@RequestBody CulturalEventImageRequestDTO culturalEventImageRequestDTO) {
        List<CulturalEventImageResponseDTO> imageList = culturalEventService.searchImage(culturalEventImageRequestDTO.getKeyword());
        return ResponseEntity.ok(ApiResponse.success("이미지 검색 성공", imageList));
    }

    // 기간에 맞게 설정
    @GetMapping("/findculturalfromdate")
    public ResponseEntity<?> findCulturalFromDate() {
        // 이미지, 제목, 장소, 시작 날짜, 끝나는 날짜
        return ResponseEntity.ok("a");
    }


    // 행사 추천(온보딩 기반, 없으면 랜덤)
    @GetMapping("/recommendcultural")
    public ResponseEntity<?> recommendCultural(@AuthenticationPrincipal UserDetails userDetails) {
        // 이미지, 제목, 장소
        List<RecommendCulturalEventResponseDTO> recommendCulturalEventResponseDTOList = culturalEventService.getRecommendCultural(userDetails.getUsername());
        if (recommendCulturalEventResponseDTOList.isEmpty()) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "데이터 찾기 실패"));
        }
        return ResponseEntity.ok(ApiResponse.success("데이터 찾기 성공", recommendCulturalEventResponseDTOList));
    }

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
    @GetMapping("/latestcultural")
    public ResponseEntity<?> latestCultural() {
        // 이미지, 제목, 장소, 기간
        LatestCulturalEventDTO latestCulturalEventDTO = culturalEventService.getLatestCulturalEvent();
        return ResponseEntity.ok(ApiResponse.success("성공", latestCulturalEventDTO));
    }

    // 행사 상세 페이지
    @GetMapping("/culturaldetail")
    public ResponseEntity<?> culturalDetail(@RequestBody CulturalEventDetailRequestDTO culturalEventDetailRequestDTO) {
        CulturalEvent culturalEvent = culturalEventService.getCulturalEvent(culturalEventDetailRequestDTO.getId());
        if (culturalEvent == null) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "데이터 검색 실패"));
        }
        return ResponseEntity.ok(ApiResponse.success("데이터 검색 성공", culturalEvent));
    }

}
