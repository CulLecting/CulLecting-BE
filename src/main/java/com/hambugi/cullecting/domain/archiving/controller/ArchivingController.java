package com.hambugi.cullecting.domain.archiving.controller;

import com.hambugi.cullecting.domain.archiving.dto.*;
import com.hambugi.cullecting.domain.archiving.service.ArchivingService;
import com.hambugi.cullecting.domain.archiving.service.GPTService;
import com.hambugi.cullecting.domain.member.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/archivings")
public class ArchivingController {

    private final ArchivingService archivingService;
    private final GPTService gptService;

    public ArchivingController(ArchivingService archivingService, GPTService gptService) {
        this.archivingService = archivingService;
        this.gptService = gptService;
    }

    // 아카이빙 업로드
    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("image") MultipartFile image, @ModelAttribute ArchivingRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("{}의 아카이빙 업로드 요청", userDetails.getUsername());
        boolean result = archivingService.addArchiving(dto, image, userDetails);
        if (!result) {
            log.warn("{}의 아카이빙 업로드 실패", userDetails.getUsername());
            return ResponseEntity.status(500).body(ApiResponse.error(500, "아카이빙 업로드에 실패했습니다."));
        }
        log.info("{}의 업로드 요청 성공", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("아카이빙 업로드 성공", null));
    }

    // 아카이빙 삭제
    @DeleteMapping("/{archivingId}")
    public ResponseEntity<?> deleteArchiving(@PathVariable String archivingId) {
        log.info("ID={} : 아카이빙 삭제 요청", archivingId);
        archivingService.deleteArchiving(archivingId);
        return ResponseEntity.ok(ApiResponse.success("아카이빙 삭제 성공", null));
    }

    // 작성한 아카이빙 리스트 받아오기
    @GetMapping
    public ResponseEntity<?> findArchiving(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("{}의 아카이빙 전체 리스트 요청", userDetails.getUsername());
        List<ArchivingResponseDTO> list = archivingService.findArchivingByMemberEmail(userDetails.getUsername());
        if (list.isEmpty()) {
            log.info("{}의 아카이빙 전체 리스트가 비어있음", userDetails.getUsername());
            return ResponseEntity.status(404).body(ApiResponse.error(404, "등록된 아카이빙 데이터가 없습니다."));
        }
        log.info("{}의 아카이빙 전체 리스트 조회 성공", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("아카이빙 목록 조회 성공", list));
    }

    // 아카이빙 단건 데이터 가져오기(id값으로)
    @GetMapping("/{archivingId}")
    public ResponseEntity<?> findArchiving(@PathVariable String archivingId) {
        log.info("ID={} : 아카이빙 단건 데이터 요청", archivingId);
        ArchivingResponseDTO dto = archivingService.findArchiving(archivingId);
        if (dto == null) {
            log.warn("ID={} : 아카이빙 데이터가 존재하지 않음", archivingId);
            return ResponseEntity.status(404).body(ApiResponse.error(404, "해당 아카이빙을 찾을 수 없습니다."));
        }
        log.info("ID={} : 아카이빙 단건 데이터 조회 성공", archivingId);
        return ResponseEntity.ok(ApiResponse.success("아카이빙 조회 성공", dto));
    }

    // 아카이빙 이미지 변경
    @PatchMapping("/{archivingId}/image")
    public ResponseEntity<?> updateArchivingImage(@RequestParam("image") MultipartFile image, @PathVariable String archivingId) {
        log.info("ID={} : 아카이빙 이미지 업데이트 요청", archivingId);
        boolean result = archivingService.updateArchivingImage(image, archivingId);
        if (!result) {
            log.warn("ID={} : 아카이빙 이미지 변경 실패", archivingId);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "이미지 수정에 실패했습니다."));
        }
        log.info("ID={} : 아카이빙 이미지 변경 성공", archivingId);
        return ResponseEntity.ok(ApiResponse.success("이미지 수정 성공", null));
    }

    // 아카이빙 정보 변경
    @PatchMapping("/{archivingId}")
    public ResponseEntity<?> updateArchiving(@PathVariable String archivingId, @RequestBody ArchivingUpdateRequestDTO dto) {
        log.info("ID={} : 아카이빙 정보 업데이트 요청", archivingId);
        boolean result = archivingService.updateArchiving(dto);
        if (!result) {
            log.warn("ID={} : 아카이빙 정보 업데이트 실패", archivingId);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "아카이빙 정보 수정에 실패했습니다."));
        }
        log.info("ID={} : 아카이빙 정보 수정 성공", archivingId);
        return ResponseEntity.ok(ApiResponse.success("아카이빙 정보 수정 성공", null));
    }

    // 아카이빙 템플릿 변경
    @PatchMapping("/{archivingId}/template")
    public ResponseEntity<?> updateArchivingTemplate(@PathVariable String archivingId, @RequestBody ArchivingTemplateUpdateRequestDTO dto) {
        log.info("ID={} : 아카이빙 템플릿 변경 요청", archivingId);
        boolean result = archivingService.updateArchivingTemplate(dto);
        if (!result) {
            log.warn("ID={} : 아카이빙 템플릿 변경 실패", archivingId);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "템플릿 수정에 실패했습니다."));
        }
        log.info("ID={} : 아카이빙 템플릿 변경 완료", archivingId);
        return ResponseEntity.ok(ApiResponse.success("템플릿 수정 성공", null));
    }

    // GPT로 데이터 분석한 것 보내주기
    @GetMapping("/preference-card")
    public ResponseEntity<?> preferenceCard(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("{}의 GPT 분석결과 및 데이터 요청", userDetails.getUsername());
        PreferenceCardDTO preferenceCardDTO = archivingService.findPreferenceCardByMemberId(userDetails.getUsername());
        if (preferenceCardDTO == null) {
            log.warn("{}의 GPT 분석 혹은 데이터 조회 실패", userDetails.getUsername());
            return ResponseEntity.status(500).body(ApiResponse.error(500, "알 수 없는 이유로 인해 데이터 불러오기에 실패했습니다."));
        }
        log.info("{}의 GPT 분석결과 및 데이터 조회 성공", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("데이터 찾기 성공", preferenceCardDTO));
    }

    // iOS 기반 더미데이터 만들기
    @PostMapping("/ios")
    public ResponseEntity<?> upload(@RequestParam("image") MultipartFile image, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("{}의 iOS 기반 더미데이터 생성 요청", userDetails.getUsername());
        String id = archivingService.addArchiving(image, userDetails);
        if (id == null) {
            log.warn("{}의 iOS 기반 더미데이터 생성 실패", userDetails.getUsername());
            return ResponseEntity.status(500).body(ApiResponse.error(500, "아카이빙 더미데이터 업로드에 실패했습니다."));
        }
        iOSArchivingAddResponse archivingAddResponse = new iOSArchivingAddResponse();
        archivingAddResponse.setId(id);
        log.info("{}의 iOS 기반 더미데이터 생성 성공", userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("아카이빙 더미데이터 업로드 성공", archivingAddResponse));
    }
}
