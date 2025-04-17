package com.hambugi.cullecting.domain.curtural.service;

import com.hambugi.cullecting.domain.curtural.dto.CulturalEventFromDateDTO;
import com.hambugi.cullecting.domain.curtural.dto.CulturalEventImageResponseDTO;
import com.hambugi.cullecting.domain.curtural.dto.LatestCulturalEventDTO;
import com.hambugi.cullecting.domain.curtural.entity.CulturalEvent;
import com.hambugi.cullecting.domain.curtural.repository.CulturalEventRepository;
import com.hambugi.cullecting.domain.curtural.util.CodeNameEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<CulturalEventFromDateDTO> getLatestCulturalList() {
        List<CulturalEvent> culturalEventList = culturalEventRepository.findTop3RecentEventsBeforeNow();
        List<CulturalEventFromDateDTO> culturalEventFromDateDTOList = new ArrayList<>();
        for (CulturalEvent culturalEvent : culturalEventList) {
            CulturalEventFromDateDTO eventFromDateDTO = new CulturalEventFromDateDTO();
            eventFromDateDTO.setTitle(culturalEvent.getTitle());
            eventFromDateDTO.setPlace(culturalEvent.getPlace());
            eventFromDateDTO.setImageURL(culturalEvent.getMainImg());
            eventFromDateDTO.setStartDate(culturalEvent.getStartDate());
            eventFromDateDTO.setEndDate(culturalEvent.getEndDate());
            culturalEventFromDateDTOList.add(eventFromDateDTO);
        }
        return culturalEventFromDateDTOList;
    }

    public List<CulturalEventFromDateDTO> getLatestCulturalFromTheme(List<String> codeNameList) {
        List<CulturalEvent> culturalEventList = culturalEventRepository.findTop3ByThemeCodeBeforeNow(codeNameList);
        List<CulturalEventFromDateDTO> culturalEventFromDateDTOList = new ArrayList<>();
        for (CulturalEvent culturalEvent : culturalEventList) {
            CulturalEventFromDateDTO eventFromDateDTO = new CulturalEventFromDateDTO();
            eventFromDateDTO.setTitle(culturalEvent.getTitle());
            eventFromDateDTO.setPlace(culturalEvent.getPlace());
            eventFromDateDTO.setImageURL(culturalEvent.getMainImg());
            eventFromDateDTO.setStartDate(culturalEvent.getStartDate());
            eventFromDateDTO.setEndDate(culturalEvent.getEndDate());
            culturalEventFromDateDTOList.add(eventFromDateDTO);
        }
        return culturalEventFromDateDTOList;
    }

    public LatestCulturalEventDTO getLatestCulturalEvent() {
        LatestCulturalEventDTO latestCulturalEventDTO = new LatestCulturalEventDTO();
        Map<String, List<CulturalEventFromDateDTO>> culturalEventFromDateDTOMap = new HashMap<>();
        culturalEventFromDateDTOMap.put("전체", getLatestCulturalList());
        for (CodeNameEnum codeNameEnum : CodeNameEnum.values()) {
            culturalEventFromDateDTOMap.put(codeNameEnum.getLabel(), getLatestCulturalFromTheme(codeNameEnum.getData()));
        }
        latestCulturalEventDTO.setResult(culturalEventFromDateDTOMap);
        return latestCulturalEventDTO;
    }
}
