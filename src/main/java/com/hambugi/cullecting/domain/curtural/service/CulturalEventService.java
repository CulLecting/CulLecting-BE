package com.hambugi.cullecting.domain.curtural.service;

import com.hambugi.cullecting.domain.archiving.service.ArchivingService;
import com.hambugi.cullecting.domain.curtural.dto.CulturalEventImageResponseDTO;
import com.hambugi.cullecting.domain.curtural.entity.CulturalEvent;
import com.hambugi.cullecting.domain.curtural.repository.CulturalEventRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CulturalEventService {
    private final CulturalEventRepository culturalEventRepository;

    public CulturalEventService(CulturalEventRepository culturalEventRepository) {
        this.culturalEventRepository = culturalEventRepository;
    }

    public List<CulturalEventImageResponseDTO> searchImage(String keyword) {
        List<CulturalEventImageResponseDTO> culturalEventImageResponseDTOList = new ArrayList<>();
        List<CulturalEvent> culturalEventList = culturalEventRepository.findByTitleContainingIgnoreCase(keyword);
        for (CulturalEvent culturalEvent : culturalEventList) {
            CulturalEventImageResponseDTO imageResponseDTO = new CulturalEventImageResponseDTO();
            imageResponseDTO.setTitle(culturalEvent.getTitle());
            imageResponseDTO.setImageURL(culturalEvent.getMainImg());
            culturalEventImageResponseDTOList.add(imageResponseDTO);
        }
        return culturalEventImageResponseDTOList;
    }
}
