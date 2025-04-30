package com.hambugi.cullecting.domain.curtural.controller;

import com.hambugi.cullecting.domain.curtural.dto.*;
import com.hambugi.cullecting.domain.curtural.entity.CulturalEvent;
import com.hambugi.cullecting.domain.curtural.service.CulturalEventService;
import com.hambugi.cullecting.domain.member.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cultural")
public class CulturalEventController {
    private final CulturalEventService culturalEventService;

    public CulturalEventController(CulturalEventService culturalEventService) {
        this.culturalEventService = culturalEventService;
    }

    // 문화 이미지 검색
    @GetMapping("/images")
    public ResponseEntity<?> findCulturalImage(@RequestParam String keyword) {
        log.info("{}가 제목에 속한 문화 이미지 검색 요청", keyword);
        List<CulturalEventImageResponseDTO> imageList = culturalEventService.searchImage(keyword);
        if (imageList.isEmpty()){
            log.info("문화 이미지 리스트가 비어있음");
            return ResponseEntity.status(404).body(ApiResponse.error(404, "해당 키워드의 문화 이미지를 찾을 수 없습니다."));
        }
        log.info("문화 이미지 리스트 조회 성공");
        return ResponseEntity.ok(ApiResponse.success("문화 이미지 검색 성공", imageList));
    }

    // 기간에 맞게 설정
    // 날짜를 넣으면 그 날짜에 하는 데이터 보내주기
    @GetMapping("/date")
    public ResponseEntity<?> findCulturalFromDate(@RequestParam LocalDate date) {
        log.info("{} 에 하는 문화 데이터 요청", date);
        List<CulturalEventResponseDTO> result = culturalEventService.getCulturalListFromDate(date);
        if (result.isEmpty()) {
            log.info("{} 에 하는 문화행사 없음", date);
            return ResponseEntity.status(404).body(ApiResponse.error(404, "해당 날짜의 문화 행사가 없습니다."));
        }
        log.info("{} 에 하는 문화행사 조회 성공", date);
        return ResponseEntity.ok(ApiResponse.success("문화 행사 조회 성공", result));
    }

    // 분야, 지역(구), 비용, 연령 검색 검색
    @GetMapping("/filter")
    public ResponseEntity<?> culturalFilter(@RequestParam(required = false) String codeName, @RequestParam(required = false) String guName, @RequestParam(required = false) String themeCode, @RequestParam(required = false) Boolean isFree) {
        log.info("문화행사 필터링 요청");
        FilterCulturalRequestDTO request = new FilterCulturalRequestDTO(codeName, guName, themeCode, isFree);
        List<CulturalEventResponseDTO> data = culturalEventService.getCulturalFilter(request);
        if (data == null || data.isEmpty()) {
            log.info("필터링 조건에 맞는 문화행사 없음");
            return ResponseEntity.status(404).body(ApiResponse.error(404, "조건에 해당하는 문화 행사가 없습니다."));
        }
        log.info("필터링 조건에 맞는 문화행사 검색 성공");
        return ResponseEntity.ok(ApiResponse.success("문화 행사 필터 조회 성공", data));
    }

    // 이름 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchCulturalName(@RequestParam String keyword) {
        log.info("제목에 {} 키워드가 포함된 문화행사 검색 요청", keyword);
        List<CulturalEventResponseDTO> result = culturalEventService.getCulturalFromKeyword(keyword);
        if (result.isEmpty()) {
            log.info("제목에 {} 키워드가 포함된 문화행사 없음 ", keyword);
            return ResponseEntity.status(404).body(ApiResponse.error(404, "해당 이름의 문화 행사를 찾을 수 없습니다."));
        }
        log.info("제목에 {} 키워드가 포함된 문화행사 조회 완료", keyword);
        return ResponseEntity.ok(ApiResponse.success("문화 행사 검색 성공", result));
    }

    // 행사 추천(온보딩 기반, 없으면 랜덤)
    @GetMapping("/recommendations")
    public ResponseEntity<?> recommendCultural(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("{}의 문화행사 추천 요청", userDetails.getUsername());
        List<RecommendCulturalEventResponseDTO> list = culturalEventService.getRecommendCultural(userDetails.getUsername());
        if (list.isEmpty()) {
            log.info("{}의 문화행사 추천 실패", userDetails.getUsername());
            return ResponseEntity.status(404).body(ApiResponse.error(404, "추천할 문화 행사가 없습니다."));
        }
        log.info("{}의 문화행사 추천 데이터 조회 성공", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("문화 행사 추천 성공", list));
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
    @GetMapping("/latest")
    public ResponseEntity<?> latestCultural() {
        log.info("최신행사 데이터 요청");
        LatestCulturalEventDTO latest = culturalEventService.getLatestCulturalEvent();
        if (latest == null) {
            log.info("최신행사 데이터 조회 실패");
            return ResponseEntity.status(404).body(ApiResponse.error(404, "최신 문화 행사를 찾을 수 없습니다."));
        }
        log.info("최신행사 데이터 조회 성공");
        return ResponseEntity.ok(ApiResponse.success("최신 문화 행사 조회 성공", latest));
    }

    // 행사 상세 페이지
    @GetMapping("/{culturalId}")
    public ResponseEntity<?> culturalDetail(@PathVariable Long culturalId) {
        log.info("ID={} : 문화행사 상세 데이터 요청", culturalId);
        CulturalEvent event = culturalEventService.getCulturalEvent(culturalId);
        if (event == null) {
            log.warn("ID={} : 문화행사가 존재하지 않음", culturalId);
            return ResponseEntity.status(404).body(ApiResponse.error(404, "해당 문화 행사를 찾을 수 없습니다."));
        }
        log.info("ID={} : 문화행사 상세 데이터 조회 완료", culturalId);
        return ResponseEntity.ok(ApiResponse.success("문화 행사 상세 조회 성공", event));
    }

}
