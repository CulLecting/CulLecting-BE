package com.hambugi.cullecting.domain.archiving.service;

import com.hambugi.cullecting.domain.archiving.dto.ArchivingRequestDTO;
import com.hambugi.cullecting.domain.archiving.dto.ArchivingResponseDTO;
import com.hambugi.cullecting.domain.archiving.dto.ArchivingTemplateUpdateRequestDTO;
import com.hambugi.cullecting.domain.archiving.dto.ArchivingUpdateRequestDTO;
import com.hambugi.cullecting.domain.archiving.entity.Archiving;
import com.hambugi.cullecting.domain.archiving.repository.ArchivingRepository;
import com.hambugi.cullecting.domain.archiving.util.CardTemplate;
import com.hambugi.cullecting.domain.member.entity.Member;
import com.hambugi.cullecting.domain.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ArchivingService {
    @Value("${my.upload-dir}")
    private String path;

    private final ArchivingRepository archivingRepository;
    private final MemberRepository memberRepository;

    public ArchivingService(ArchivingRepository archivingRepository, MemberRepository memberRepository) {
        this.archivingRepository = archivingRepository;
        this.memberRepository = memberRepository;
    }

    public boolean addArchiving(ArchivingRequestDTO archivingRequestDTO, MultipartFile image, UserDetails userDetails) {
        String imageName = addImage(image);
        Member member = memberRepository.findByEmail(userDetails.getUsername());
        if (imageName == null) {
            return false;
        }
        Archiving archiving = new Archiving();
        archiving.setTitle(archivingRequestDTO.getTitle());
        archiving.setDescription(archivingRequestDTO.getDescription());
        archiving.setDate(archivingRequestDTO.getDate());
        archiving.setImageURL("/images/" + imageName);
        archiving.setCategory(archivingRequestDTO.getCategory());
        archiving.setTemplate(CardTemplate.valueOf(archivingRequestDTO.getTemplate().toUpperCase()));
        archiving.setMember(member);
        archivingRepository.save(archiving);
        return true;
    }

    private String addImage(MultipartFile image) {
//        String fileName = image.getOriginalFilename();
//        Path filePath = Paths.get(path + fileName);
        String originalFilename = image.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + ext;

        Path filePath = Paths.get(path + uniqueFileName);
        try {
            Files.copy(image.getInputStream(), filePath);
        } catch (Exception e) {
            return null;
        }
        return uniqueFileName;
    }

    public List<ArchivingResponseDTO> findArchivingByMemberEmail(String email) {
        List<Archiving> archivingList = archivingRepository.findByMemberEmail(email);
        List<ArchivingResponseDTO> archivingResponseDTOList = new ArrayList<>();
        for (Archiving archiving : archivingList) {
            ArchivingResponseDTO archivingResponseDTO = new ArchivingResponseDTO();
            archivingResponseDTO.setId(archiving.getId());
            archivingResponseDTO.setTitle(archiving.getTitle());
            archivingResponseDTO.setDescription(archiving.getDescription());
            archivingResponseDTO.setDate(archiving.getDate());
            archivingResponseDTO.setCategory(archiving.getCategory());
            archivingResponseDTO.setTemplate(archiving.getTemplate().toString());
            archivingResponseDTO.setImageURL(archiving.getImageURL());
            archivingResponseDTOList.add(archivingResponseDTO);
        }
        return archivingResponseDTOList;
    }

    public Archiving findArchivingById(String id) {
        return archivingRepository.findById(id).orElse(null);
    }

    public boolean updateArchivingImage(MultipartFile image, String id) {
        Archiving archiving = findArchivingById(id);
        if (archiving == null) {
            return false;
        }
        String beforeImage = archiving.getImageURL();
        String beforeImageName = beforeImage.replace("/images/", "");
        // 1. 기존 이미지 삭제
        if (!beforeImageName.isBlank()) {
            Path oldFilePath = Paths.get(path + beforeImageName);
            System.out.println(oldFilePath);
            try {
                Files.deleteIfExists(oldFilePath);
            } catch (IOException e) {
                System.out.println("⚠️ 기존 이미지 삭제 실패: " + e.getMessage());
            }
        }
        String imageName = addImage(image);
        archiving.setImageURL("/images/" + imageName);
        archivingRepository.save(archiving);
        return true;
    }

    public boolean updateArchiving(ArchivingUpdateRequestDTO archivingUpdateRequestDTO) {
        Archiving archiving = findArchivingById(archivingUpdateRequestDTO.getId());
        if (archiving == null) {
            return false;
        }
        archiving.setTitle(archivingUpdateRequestDTO.getTitle());
        archiving.setDescription(archivingUpdateRequestDTO.getDescription());
        archiving.setDate(archivingUpdateRequestDTO.getDate());
        archiving.setCategory(archivingUpdateRequestDTO.getCategory());
        archivingRepository.save(archiving);
        return true;
    }

    public boolean updateArchivingTemplate(ArchivingTemplateUpdateRequestDTO archivingTemplateUpdateRequestDTO) {
        Archiving archiving = findArchivingById(archivingTemplateUpdateRequestDTO.getId());
        if (archiving == null) {
            return false;
        }
        archiving.setTemplate(CardTemplate.valueOf(archivingTemplateUpdateRequestDTO.getTemplate().toUpperCase()));
        archivingRepository.save(archiving);
        return true;
    }

    public void deleteArchiving(String id) {
        archivingRepository.deleteById(id);
    }
}
