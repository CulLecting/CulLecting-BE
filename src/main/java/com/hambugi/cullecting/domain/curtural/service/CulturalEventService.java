package com.hambugi.cullecting.domain.curtural.service;

import com.hambugi.cullecting.domain.curtural.dto.*;
import com.hambugi.cullecting.domain.curtural.entity.CulturalEvent;
import com.hambugi.cullecting.domain.curtural.repository.CulturalEventRepository;
import com.hambugi.cullecting.domain.curtural.repository.CulturalEventSpecifications;
import com.hambugi.cullecting.domain.curtural.util.CodeNameEnum;
import com.hambugi.cullecting.domain.member.entity.Member;
import com.hambugi.cullecting.domain.member.service.MemberService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CulturalEventService {
    private final CulturalEventRepository culturalEventRepository;
    private final MemberService memberService;

    public CulturalEventService(CulturalEventRepository culturalEventRepository, MemberService memberService) {
        this.culturalEventRepository = culturalEventRepository;
        this.memberService = memberService;
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

    public List<CulturalEventResponseDTO> getLatestCulturalList() {
        List<CulturalEvent> culturalEventList = culturalEventRepository.findTop3RecentEventsBeforeNow();
        List<CulturalEventResponseDTO> culturalEventResponseDTOList = new ArrayList<>();
        for (CulturalEvent culturalEvent : culturalEventList) {
            CulturalEventResponseDTO eventFromDateDTO = new CulturalEventResponseDTO();
            eventFromDateDTO.setId(culturalEvent.getId());
            eventFromDateDTO.setTitle(culturalEvent.getTitle());
            eventFromDateDTO.setPlace(culturalEvent.getPlace());
            eventFromDateDTO.setImageURL(culturalEvent.getMainImg());
            eventFromDateDTO.setStartDate(culturalEvent.getStartDate());
            eventFromDateDTO.setEndDate(culturalEvent.getEndDate());
            culturalEventResponseDTOList.add(eventFromDateDTO);
        }
        return culturalEventResponseDTOList;
    }

    public List<CulturalEventResponseDTO> getLatestCulturalFromTheme(List<String> codeNameList) {
        List<CulturalEvent> culturalEventList = culturalEventRepository.findTop3ByThemeCodeBeforeNow(codeNameList);
        List<CulturalEventResponseDTO> culturalEventResponseDTOList = new ArrayList<>();
        for (CulturalEvent culturalEvent : culturalEventList) {
            CulturalEventResponseDTO eventFromDateDTO = new CulturalEventResponseDTO();
            eventFromDateDTO.setId(culturalEvent.getId());
            eventFromDateDTO.setTitle(culturalEvent.getTitle());
            eventFromDateDTO.setPlace(culturalEvent.getPlace());
            eventFromDateDTO.setImageURL(culturalEvent.getMainImg());
            eventFromDateDTO.setStartDate(culturalEvent.getStartDate());
            eventFromDateDTO.setEndDate(culturalEvent.getEndDate());
            culturalEventResponseDTOList.add(eventFromDateDTO);
        }
        return culturalEventResponseDTOList;
    }

    public LatestCulturalEventDTO getLatestCulturalEvent() {
        LatestCulturalEventDTO latestCulturalEventDTO = new LatestCulturalEventDTO();
        Map<String, List<CulturalEventResponseDTO>> culturalEventFromDateDTOMap = new HashMap<>();
        culturalEventFromDateDTOMap.put("전체", getLatestCulturalList());
        for (CodeNameEnum codeNameEnum : CodeNameEnum.values()) {
            culturalEventFromDateDTOMap.put(codeNameEnum.getLabel(), getLatestCulturalFromTheme(codeNameEnum.getData()));
        }
        latestCulturalEventDTO.setResult(culturalEventFromDateDTOMap);
        return latestCulturalEventDTO;
    }

    public List<RecommendCulturalEventResponseDTO> getRecommendCultural(String email) {
        Member member = memberService.findByEmail(email);
        if (member == null) {
            // 유저가 없는 경우
            return null;
        }
        List<CulturalEvent> culturalEventList = new ArrayList<>();
        if (member.getCategoryList().isEmpty()) {
            // 유저가 온보딩을 건너 뛰었을 경우
            culturalEventList = culturalEventRepository.findTop10Random();
        } else {
            for (String code : member.getCategoryList()) {
                if (code.equals("축제")) {
                    // 축제 관련(축제/야외체험, 문화예술)
                    List<String> codeNameList = new ArrayList<>();
                    codeNameList.addAll(CodeNameEnum.FESTIVAL.getData());
                    codeNameList.addAll(CodeNameEnum.CULTURAL.getData());
                    culturalEventList.addAll(culturalEventRepository.findTop3ByThemeCodeBeforeNow(codeNameList));
                } else {
                    for (CodeNameEnum codeNameEnum : CodeNameEnum.values()) {
                        if (codeNameEnum.getData().contains(code)) {
                            culturalEventList.addAll(culturalEventRepository.findTop3ByThemeCodeBeforeNow(codeNameEnum.getData()));
                            break;
                        }
                    }
                }
            }
        }
        List<RecommendCulturalEventResponseDTO> recommendCulturalEventResponseDTOList = new ArrayList<>();
        for (CulturalEvent culturalEvent : culturalEventList) {
            RecommendCulturalEventResponseDTO responseDTO = new RecommendCulturalEventResponseDTO();
            responseDTO.setId(culturalEvent.getId());
            responseDTO.setTitle(culturalEvent.getTitle());
            responseDTO.setPlace(culturalEvent.getPlace());
            responseDTO.setImageURL(culturalEvent.getMainImg());
            recommendCulturalEventResponseDTOList.add(responseDTO);
        }
        return recommendCulturalEventResponseDTOList;
    }

    public CulturalEvent getCulturalEvent(Long id) {
        return culturalEventRepository.findById(id);
    }

    public List<CulturalEventResponseDTO> getCulturalListFromDate(LocalDate date) {
        List<CulturalEvent> culturalEventList = culturalEventRepository.findCulturalFromDate(date);
        if (culturalEventList == null) {
            return null;
        }
        List<CulturalEventResponseDTO> culturalEventResponseDTOList = new ArrayList<>();
        for (CulturalEvent culturalEvent : culturalEventList) {
            CulturalEventResponseDTO responseDTO = new CulturalEventResponseDTO();
            responseDTO.setId(culturalEvent.getId());
            responseDTO.setTitle(culturalEvent.getTitle());
            responseDTO.setPlace(culturalEvent.getPlace());
            responseDTO.setImageURL(culturalEvent.getMainImg());
            responseDTO.setStartDate(culturalEvent.getStartDate());
            responseDTO.setEndDate(culturalEvent.getEndDate());
            culturalEventResponseDTOList.add(responseDTO);
        }
        return culturalEventResponseDTOList;
    }

    public List<CulturalEventResponseDTO> getCulturalFilter(FilterCulturalRequestDTO filterCulturalRequestDTO) {
        Specification<CulturalEvent> spec = Specification
                .where(CulturalEventSpecifications.withCodename(filterCulturalRequestDTO.getCodeName()))
                .and(CulturalEventSpecifications.withGuname(filterCulturalRequestDTO.getGuName()))
                .and(CulturalEventSpecifications.withIsFree(filterCulturalRequestDTO.getIsFree()))
                .and(CulturalEventSpecifications.withThemeCode(filterCulturalRequestDTO.getThemeCode()));
        List<CulturalEvent> culturalEventList = culturalEventRepository.findAll(spec);
        if (culturalEventList.isEmpty()) {
            return null;
        }
        List<CulturalEventResponseDTO> culturalEventResponseDTOList = new ArrayList<>();
        for (CulturalEvent culturalEvent : culturalEventList) {
            CulturalEventResponseDTO responseDTO = new CulturalEventResponseDTO();
            responseDTO.setId(culturalEvent.getId());
            responseDTO.setTitle(culturalEvent.getTitle());
            responseDTO.setPlace(culturalEvent.getPlace());
            responseDTO.setImageURL(culturalEvent.getMainImg());
            responseDTO.setStartDate(culturalEvent.getStartDate());
            responseDTO.setEndDate(culturalEvent.getEndDate());
            culturalEventResponseDTOList.add(responseDTO);
        }
        return culturalEventResponseDTOList;
    }

    public List<CulturalEventResponseDTO> getCulturalFromKeyword(String keyword) {
        List<CulturalEvent> culturalEventList = culturalEventRepository.findByTitleContainingIgnoreCase(keyword);
        List<CulturalEventResponseDTO> culturalEventResponseDTOList = new ArrayList<>();
        for (CulturalEvent culturalEvent : culturalEventList) {
            CulturalEventResponseDTO responseDTO = new CulturalEventResponseDTO();
            responseDTO.setId(culturalEvent.getId());
            responseDTO.setTitle(culturalEvent.getTitle());
            responseDTO.setPlace(culturalEvent.getPlace());
            responseDTO.setImageURL(culturalEvent.getMainImg());
            responseDTO.setStartDate(culturalEvent.getStartDate());
            responseDTO.setEndDate(culturalEvent.getEndDate());
            culturalEventResponseDTOList.add(responseDTO);
        }
        return culturalEventResponseDTOList;
    }
}
