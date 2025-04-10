package com.hambugi.cullecting.domain.archiving.service;

import com.hambugi.cullecting.domain.archiving.dto.ArchivingRequestDTO;
import com.hambugi.cullecting.domain.archiving.dto.ArchivingResponseDTO;
import com.hambugi.cullecting.domain.archiving.entity.Archiving;
import com.hambugi.cullecting.domain.archiving.repository.ArchivingRepository;
import com.hambugi.cullecting.domain.archiving.util.CardTemplate;
import com.hambugi.cullecting.domain.member.entity.Member;
import com.hambugi.cullecting.domain.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
        String fileName = image.getOriginalFilename();
        Path filePath = Paths.get(path + fileName);
        try {
            Files.copy(image.getInputStream(), filePath);
        } catch (Exception e) {
            return null;
        }
        return fileName;
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
}
