package com.hambugi.cullecting.domain.archiving.controller;

import com.hambugi.cullecting.domain.archiving.dto.ArchivingRequestDTO;
import com.hambugi.cullecting.domain.archiving.dto.ArchivingResponseDTO;
import com.hambugi.cullecting.domain.archiving.entity.Archiving;
import com.hambugi.cullecting.domain.archiving.service.ArchivingService;
import com.hambugi.cullecting.domain.member.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/archiving")
public class ArchivingController {

    private final ArchivingService archivingService;

    public ArchivingController(ArchivingService archivingService) {
        this.archivingService = archivingService;
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

}
