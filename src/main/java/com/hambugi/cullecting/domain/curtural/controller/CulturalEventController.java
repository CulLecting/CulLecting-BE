package com.hambugi.cullecting.domain.curtural.controller;

import com.hambugi.cullecting.domain.curtural.dto.*;
import com.hambugi.cullecting.domain.curtural.entity.CulturalEvent;
import com.hambugi.cullecting.domain.curtural.service.CulturalEventService;
import com.hambugi.cullecting.domain.member.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public ResponseEntity<?> findCulturalImage(@RequestParam String keyword) {
        List<CulturalEventImageResponseDTO> imageList = culturalEventService.searchImage(keyword);
        return ResponseEntity.ok(ApiResponse.success("이미지 검색 성공", imageList));
    }

    // 기간에 맞게 설정
    // 날짜를 넣으면 그 날짜에 하는 데이터 보내주기
    @GetMapping("/findculturalfromdate")
    public ResponseEntity<?> findCulturalFromDate(@RequestParam LocalDate date) {
        List<CulturalEventResponseDTO> responseDTOList = culturalEventService.getCulturalListFromDate(date);
        if (responseDTOList.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("데이터가 존재하지 않음", null));
        }
        return ResponseEntity.ok(ApiResponse.success("데이터 검색 완료", responseDTOList));
        // 이미지, 제목, 장소, 시작 날짜, 끝나는 날짜
    }

    // 분야, 지역(구), 비용, 연령 검색 검색
    @GetMapping("/culturalfilter")
    public ResponseEntity<?> culturalFilter(@RequestParam(required = false) String codeName, @RequestParam(required = false) String guName, @RequestParam(required = false) String themeCode, @RequestParam(required = false) Boolean isFree) {
        FilterCulturalRequestDTO request = new FilterCulturalRequestDTO();
        request.setCodeName(codeName);
        request.setGuName(guName);
        request.setThemeCode(themeCode);
        request.setIsFree(isFree);
        List<CulturalEventResponseDTO> data = culturalEventService.getCulturalFilter(request);
        if (data == null || data.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("데이터 없음" , null));
        }
        return ResponseEntity.ok(ApiResponse.success("데이터 검색 완료", data));
    }

    // 이름 검색
    @GetMapping("/findculturalname")
    public ResponseEntity<?> findCulturalName(@RequestParam String keyword) {
        List<CulturalEventResponseDTO> culturalEventResponseDTOList = culturalEventService.getCulturalFromKeyword(keyword);
        if (culturalEventResponseDTOList.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("데이터가 존재하지 않음", null));
        }
        return ResponseEntity.ok(ApiResponse.success("데이터 검색 완료", culturalEventResponseDTOList));
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
    public ResponseEntity<?> culturalDetail(@RequestParam Long culturalId) {
        CulturalEvent culturalEvent = culturalEventService.getCulturalEvent(culturalId);
        if (culturalEvent == null) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "데이터 검색 실패"));
        }
        return ResponseEntity.ok(ApiResponse.success("데이터 검색 성공", culturalEvent));
    }

}
