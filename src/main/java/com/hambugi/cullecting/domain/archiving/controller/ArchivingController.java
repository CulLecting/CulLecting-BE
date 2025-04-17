package com.hambugi.cullecting.domain.archiving.controller;

import com.hambugi.cullecting.domain.archiving.dto.*;
import com.hambugi.cullecting.domain.archiving.entity.Archiving;
import com.hambugi.cullecting.domain.archiving.service.ArchivingService;
import com.hambugi.cullecting.domain.archiving.service.GPTService;
import com.hambugi.cullecting.domain.member.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/archiving")
public class ArchivingController {

    private final ArchivingService archivingService;
    private final GPTService gptService;

    public ArchivingController(ArchivingService archivingService, GPTService gptService) {
        this.archivingService = archivingService;
        this.gptService = gptService;
    }

    // 아카이빙 업로드
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("image") MultipartFile image, @ModelAttribute ArchivingRequestDTO archivingRequestDTO, @AuthenticationPrincipal UserDetails userDetails) {
        boolean result = archivingService.addArchiving(archivingRequestDTO, image, userDetails);
        if (!result) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "아카이빙 추가 실패"));
        }
        return ResponseEntity.ok(ApiResponse.success("추가 성공", null));
    }

    // 작성한 아카이빙 리스트 받아오기
    @GetMapping("/findarchiving")
    public ResponseEntity<?> findArchiving(@AuthenticationPrincipal UserDetails userDetails) {
        List<ArchivingResponseDTO> archivingList = archivingService.findArchivingByMemberEmail(userDetails.getUsername());
        if (archivingList.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("데이터가 없습니다.", null));
        }
        return ResponseEntity.ok(ApiResponse.success("조회 완료", archivingList));
    }

    // 아카이빙 이미지 변경
    @PostMapping("/updateimage")
    public ResponseEntity<?> updateArchivingImage(@RequestParam("image") MultipartFile image, @ModelAttribute ArchivingImageUpdateRequestDTO archivingImageUpdateRequestDTO) {
        boolean result = archivingService.updateArchivingImage(image, archivingImageUpdateRequestDTO.getId());
        if (!result) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "이미지 업데이트 실패"));
        }
        return ResponseEntity.ok(ApiResponse.success("이미지 업데이트 성공", null));
    }

    // 아카이빙 정보 변경
    @PostMapping("/update")
    public ResponseEntity<?> updateArchiving(@RequestBody ArchivingUpdateRequestDTO archivingUpdateRequestDTO) {
        boolean result = archivingService.updateArchiving(archivingUpdateRequestDTO);
        if (!result) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "아카이빙 업데이트 실패"));
        }
        return ResponseEntity.ok(ApiResponse.success("아카이빙 업데이트 성공", null));
    }

    // 아카이빙 템플릿 변경
    @PostMapping("/updatetemplate")
    public ResponseEntity<?> updateArchivingTemplate(@RequestBody ArchivingTemplateUpdateRequestDTO archivingTemplateUpdateRequestDTO) {
        boolean result = archivingService.updateArchivingTemplate(archivingTemplateUpdateRequestDTO);
        if (!result) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "템플릿 변경 실패"));
        }
        return ResponseEntity.ok(ApiResponse.success("템플릿 변경 성공", null));
    }

    // 아카이빙 삭제
    @PostMapping("/deletearchiving")
    public ResponseEntity<?> deleteArchiving(@RequestBody ArchivingDeleteRequestDTO archivingDeleteRequestDTO) {
        archivingService.deleteArchiving(archivingDeleteRequestDTO.getId());
        return ResponseEntity.ok(ApiResponse.success("아카이빙 삭제 성공", null));
    }

    @PostMapping("/get-preference-card")
    public ResponseEntity<?> preferenceCard(@AuthenticationPrincipal UserDetails userDetails) {
        PreferenceCardDTO preferenceCardDTO = archivingService.findPreferenceCardByMemberId(userDetails.getUsername());
        if (preferenceCardDTO == null) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "알 수 없는 오류로 인해 찾지 못함"));
        }
        return ResponseEntity.ok(ApiResponse.success("데이터 찾기 성공", preferenceCardDTO));
    }

}
