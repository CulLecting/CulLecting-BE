package com.hambugi.cullecting.domain.curtural.controller;

import com.hambugi.cullecting.domain.curtural.dto.CulturalEventImageRequest;
import com.hambugi.cullecting.domain.curtural.dto.CulturalEventImageResponseDTO;
import com.hambugi.cullecting.domain.curtural.service.CulturalEventService;
import com.hambugi.cullecting.domain.member.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> findCulturalImage(@RequestBody CulturalEventImageRequest culturalEventImageRequest) {
        List<CulturalEventImageResponseDTO> imageList = culturalEventService.searchImage(culturalEventImageRequest.getKeyword());
        return ResponseEntity.ok(ApiResponse.success("이미지 검색 성공", imageList));
    }
}
